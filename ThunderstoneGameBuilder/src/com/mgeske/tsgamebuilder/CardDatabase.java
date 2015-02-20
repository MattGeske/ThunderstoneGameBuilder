package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;
import com.mgeske.tsgamebuilder.requirement.CardTypeRequirement;
import com.mgeske.tsgamebuilder.requirement.HasAllClassesRequirement;
import com.mgeske.tsgamebuilder.requirement.HasAnyAttributesRequirement;
import com.mgeske.tsgamebuilder.requirement.HasAnyClassesRequirement;
import com.mgeske.tsgamebuilder.requirement.HasStrengthRequirement;
import com.mgeske.tsgamebuilder.requirement.LightweightEdgedWeaponRequirement;
import com.mgeske.tsgamebuilder.requirement.Requirement;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class CardDatabase extends SQLiteAssetHelper {
	private static final String DATABASE_NAME = "cards.sqlite";
	private static final int DATABASE_VERSION = 1;
	private static Map<String,Requirement> requirementsCache = null;
	
	public CardDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public List<String> getThunderstoneSets() {
		String[] columns = {"setName", "abbreviation", "releaseOrder"};
		String tables = "ThunderstoneSet";
		String sortOrder = "releaseOrder ASC";
		
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tables);
			c = queryBuilder.query(db, columns, null, null, null, null, sortOrder);
			List<String> thunderstoneSets = new ArrayList<String>();
			int setNameIndex = c.getColumnIndexOrThrow("setName");
			while(c.moveToNext()) {
				String setName = c.getString(setNameIndex);
				thunderstoneSets.add(setName);
			}
			return thunderstoneSets;
		} finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	private Map<String,Requirement> getRequirements() {
		if(requirementsCache == null) {
			initializeRequirementsCache();
		}
		return requirementsCache;
	}
	
	private void initializeRequirementsCache() {
		requirementsCache = new HashMap<String,Requirement>();
		
		String table = "Requirement";
		String[] columns = {"requirementName", "requirementType", "requirementValues", "requiredOn"};
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(table);
			c = queryBuilder.query(db, columns, null, null, null, null, null, null);
			while(c.moveToNext()) {
				String requirementName = c.getString(c.getColumnIndexOrThrow("requirementName"));
				String requirementType = c.getString(c.getColumnIndexOrThrow("requirementType"));
				if(requirementType == null) {
					continue;
				}
				String raw_values = c.getString(c.getColumnIndexOrThrow("requirementValues"));
				List<String> values;
				if(raw_values != null) {
					values = Arrays.asList(raw_values.split(","));
				} else {
					values = new ArrayList<String>();
				}
				String requiredOn = c.getString(c.getColumnIndexOrThrow("requiredOn"));
				
				Requirement r = Requirement.buildRequirement(requirementName, requirementType, values, requiredOn);
				requirementsCache.put(requirementName, r);
			}
		} finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	public Iterator<? extends Card> getMatchingCards(Requirement requirement, CardList currentCards) {
		RequirementQueryBuilder queryBuilder = RequirementQueryBuilder.getRequirementQueryBuilder(requirement);
		
		SQLiteDatabase db = getReadableDatabase();
		return queryBuilder.queryMatchingCards(requirement, currentCards, db, getRequirements());
	}

}

abstract class CardBuilder<T extends Card> {
	private SQLiteDatabase db;
	private Map<String, Requirement> allRequirements;

	public CardBuilder(SQLiteDatabase db, Map<String,Requirement> allRequirements) {
		this.db = db;
		this.allRequirements = allRequirements;
	}
	
	protected T buildCard(String cardId) {
		String mainTableName = getMainTableName();
		String joinTables = getJoinTables(mainTableName);
		
		List<String> columns = new ArrayList<String>();
		columns.add("cardName");
		columns.add("abbreviation as setName");
		columns.add("description");
		columns.add("group_concat(distinct className) as classes");
		columns.add("group_concat(distinct attributeName) as attributes");
		columns.add("group_concat(distinct requirementName) as requirements");
		columns.addAll(getAdditionalColumns());
		
		String selection = mainTableName+"._ID=?";
		String[] selectionArgs = new String[]{cardId};
		String groupBy = "cardName";
		String having = "releaseOrder = max(releaseOrder)";
		
		Cursor c = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(joinTables);
			c = queryBuilder.query(db, columns.toArray(new String[]{}), selection, selectionArgs, groupBy, having, null, null);
			c.moveToNext();
			return buildCard(c, allRequirements);
		} finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	protected T buildCard(Cursor c, Map<String,Requirement> allRequirements) {
		String cardName = getString(c, "cardName");
		String setName = getString(c, "setName");
		String cardDescription = getString(c, "description");
		List<String> attributes = getListFromGroupConcat(c, "attributes");
		List<String> classes = getListFromGroupConcat(c, "classes");
		List<String> requirementNames = getListFromGroupConcat(c, "requirements");
		List<Requirement> cardRequirements = new ArrayList<Requirement>();
		for(String requirementName : requirementNames) {
			cardRequirements.add(allRequirements.get(requirementName));
		}
		return buildCard(c, cardName, setName, cardDescription, attributes, classes, cardRequirements);
	}
	
	protected abstract String getMainTableName();
	protected abstract List<String> getAdditionalColumns();
	
	private String getJoinTables(String mainTableName) {
		return mainTableName+" "+
				"LEFT OUTER JOIN Card_CardClass ON "+mainTableName+"._ID = Card_CardClass.cardId and Card_CardClass.cardTableName = '"+mainTableName+"' " +
				"LEFT OUTER JOIN CardClass ON Card_CardClass.classId = CardClass._ID " +
				"LEFT OUTER JOIN Card_CardAttribute ON "+mainTableName+"._ID = Card_CardAttribute.cardId and Card_CardAttribute.cardTableName = '"+mainTableName+"' " +
				"LEFT OUTER JOIN CardAttribute ON Card_CardAttribute.attributeId = CardAttribute._ID " +
				"LEFT OUTER JOIN Card_Requirement ON "+mainTableName+"._ID = Card_Requirement.cardId and Card_Requirement.cardTableName = '"+mainTableName+"' " +
				"LEFT OUTER JOIN Requirement ON Card_Requirement.requirementId = Requirement._ID " + 
				"LEFT OUTER JOIN Card_ThunderstoneSet ON "+mainTableName+"._ID = Card_ThunderstoneSet.cardId and Card_ThunderstoneSet.CardTableName = '"+mainTableName+"' " +
				"LEFT OUTER JOIN ThunderstoneSet ON Card_ThunderstoneSet.setId = ThunderstoneSet._ID";
	}
	
	protected List<String> getListFromGroupConcat(Cursor c, String columnName) {
		String raw_value = c.getString(c.getColumnIndexOrThrow(columnName));
		if(raw_value != null) {
			return Arrays.asList(raw_value.split(","));
		} else {
			return new ArrayList<String>();
		}
	}
	
	protected Integer getInteger(Cursor c, String columnName) {
		String raw_value = c.getString(c.getColumnIndexOrThrow(columnName));
		Integer value;
		try {
			value = Integer.valueOf(raw_value);
		} catch(Exception e) {
			value = null;
		}
		return value;
	}
	
	protected int getInt(Cursor c, String columnName) {
		return c.getInt(c.getColumnIndexOrThrow(columnName));
	}
	
	protected String getString(Cursor c, String columnName) {
		return c.getString(c.getColumnIndexOrThrow(columnName));
	}

	protected abstract T buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements);
}

class DungeonCardBuilder extends CardBuilder<DungeonCard> {
	public DungeonCardBuilder(SQLiteDatabase db, Map<String, Requirement> allRequirements) {
		super(db, allRequirements);
	}

	@Override
	public DungeonCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		String cardType = getString(c, "cardType");
		Integer level = getInteger(c, "level");
		return new DungeonCard(cardName, setName, cardDescription, cardType, level, attributes, classes, cardRequirements);
	}

	@Override
	protected String getMainTableName() {
		return "DungeonCard";
	}

	@Override
	protected List<String> getAdditionalColumns() {
		List<String> additionalColumns = new ArrayList<String>();
		additionalColumns.add("cardType");
		additionalColumns.add("level");
		return additionalColumns;
	}
}

class HeroCardBuilder extends CardBuilder<HeroCard> {
	public HeroCardBuilder(SQLiteDatabase db, Map<String, Requirement> allRequirements) {
		super(db, allRequirements);
	}

	@Override
	public HeroCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		int strength = getInt(c, "strength");
		return new HeroCard(cardName, setName, cardDescription, attributes, classes, cardRequirements, strength);
	}

	@Override
	protected String getMainTableName() {
		return "HeroCard";
	}

	@Override
	protected List<String> getAdditionalColumns() {
		List<String> additionalColumns = new ArrayList<String>();
		additionalColumns.add("strength");
		return additionalColumns;
	}
}

class VillageCardBuilder extends CardBuilder<VillageCard> {
	public VillageCardBuilder(SQLiteDatabase db, Map<String, Requirement> allRequirements) {
		super(db, allRequirements);
	}

	@Override
	public VillageCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		int cost = getInt(c, "goldCost");
		Integer weight = getInteger(c, "weight");
		return new VillageCard(cardName, setName, cardDescription, attributes, classes, cardRequirements, cost, weight);
	}

	@Override
	protected String getMainTableName() {
		return "VillageCard";
	}

	@Override
	protected List<String> getAdditionalColumns() {
		List<String> additionalColumns = new ArrayList<String>();
		additionalColumns.add("goldCost");
		additionalColumns.add("weight");
		return additionalColumns;
	}
}

class ThunderstoneCardBuilder extends CardBuilder<ThunderstoneCard> {
	public ThunderstoneCardBuilder(SQLiteDatabase db, Map<String, Requirement> allRequirements) {
		super(db, allRequirements);
	}

	@Override
	protected ThunderstoneCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		return new ThunderstoneCard(cardName, setName, cardDescription, attributes, classes, cardRequirements);
	}

	@Override
	protected String getMainTableName() {
		return "DungeonBossCard";
	}

	@Override
	protected List<String> getAdditionalColumns() {
		List<String> additionalColumns = new ArrayList<String>();
		//ThunderstoneCard doesn't have any additional columns
		return additionalColumns;
	}
}

class CardResultIterator implements Iterator<Card> {
	private int nextItemPosition = 0;
	private String[] cardIds;
	private CardBuilder<? extends Card> cardBuilder;
	private Random random = new Random();
	
	public CardResultIterator(String[] cardIds, CardBuilder<? extends Card> cardBuilder) {
		this.cardIds = cardIds;
		this.cardBuilder = cardBuilder;
		
		//randomize the order of the cardIds
		for(int i = cardIds.length-1; i > 0; i--) {
			int j = random.nextInt(i+1);
			String temp = cardIds[i];
			cardIds[i] = cardIds[j];
			cardIds[j] = temp;
		}
	}

	@Override
	public boolean hasNext() {
		return nextItemPosition < cardIds.length;
	}

	@Override
	public Card next() {
		if(!hasNext()) {
			throw new NoSuchElementException("No remaining card ids");
		}
		String nextCardId = cardIds[nextItemPosition];
		nextItemPosition++;
		return cardBuilder.buildCard(nextCardId);
	}

	@Override
	public void remove() {
		//do nothing
	}
	
}

abstract class RequirementQueryBuilder {
	private static Map<String,String> tableNameMap;
	private static Map<Requirement,RequirementQueryBuilder> instanceMap = new HashMap<Requirement,RequirementQueryBuilder>();
	
	static {
		tableNameMap = new HashMap<String,String>();
		tableNameMap.put("Monster", "DungeonCard");
		tableNameMap.put("Level1Monster", "DungeonCard");
		tableNameMap.put("Level2Monster", "DungeonCard");
		tableNameMap.put("Level3Monster", "DungeonCard");
		tableNameMap.put("Thunderstone", "DungeonBossCard");
		tableNameMap.put("Hero", "HeroCard");
		tableNameMap.put("Village", "VillageCard");
	}
	
	public Iterator<? extends Card> queryMatchingCards(Requirement requirement, CardList currentCards, SQLiteDatabase db, Map<String, Requirement> allRequirements) {
		String[] cardIds = getMatchingCardIds(requirement, currentCards, db);
		
		String mainTableName = getMainTableName(requirement);
		CardBuilder<? extends Card> cardBuilder = getCardBuilder(mainTableName, db, allRequirements);
		
		return new CardResultIterator(cardIds, cardBuilder);
	}
	
	protected String getMainTableName(Requirement requirement) {
		String cardType = requirement.getRequiredOn();
		return tableNameMap.get(cardType);
	}
	
	protected String[] getMatchingCardIds(Requirement requirement, CardList currentCards, SQLiteDatabase db) {
		String tableName = getTableName(requirement);
		String cardIdColumn = getCardIdColumn();
		String[] columns = new String[]{"distinct("+cardIdColumn+")"};
		String selection = getSelection(requirement, currentCards);
		String[] selectionArgs = getSelectionArgs(requirement, currentCards);
		
		String[] matchingCardIds = null;
		Cursor c = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tableName);
			c = queryBuilder.query(db, columns, selection, selectionArgs, null, null, null, null);
			matchingCardIds = new String[c.getCount()];
			while(c.moveToNext()) {
				String cardId = c.getString(c.getColumnIndexOrThrow(cardIdColumn));
				matchingCardIds[c.getPosition()] = cardId;
			}
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return matchingCardIds;
	}
	
	protected abstract String getTableName(Requirement requirement);
	protected abstract String getSelection(Requirement requirement, CardList currentCards);
	protected abstract String[] getSelectionArgs(Requirement requirement, CardList currentCards);
	protected String getCardIdColumn() {
		return "cardId";
	}
	
	protected CardBuilder<? extends Card> getCardBuilder(String mainTableName, SQLiteDatabase db, Map<String,Requirement> allRequirements) {
		if("DungeonCard".equals(mainTableName)) {
			return new DungeonCardBuilder(db, allRequirements);
		} else if("DungeonBossCard".equals(mainTableName)) {
			return new ThunderstoneCardBuilder(db, allRequirements);
		} else if("HeroCard".equals(mainTableName)) {
			return new HeroCardBuilder(db, allRequirements);
		} else if("VillageCard".equals(mainTableName)) {
			return new VillageCardBuilder(db, allRequirements);
		} else {
			throw new RuntimeException("Couldn't determine card builder for table "+mainTableName);
		}
	}
	
	protected String buildInClausePlaceholders(int num_values) {
		StringBuilder sb = new StringBuilder(2*num_values+1);
		sb.append("(");
		for(int i = 0; i < num_values; i++) {
			if(i > 0) {
				sb.append(",");
			}
			sb.append("?");
		}
		sb.append(")");
		return sb.toString();
	}
	
	public static RequirementQueryBuilder getRequirementQueryBuilder(Requirement requirement) {
		if(instanceMap.containsKey(requirement)) {
			return instanceMap.get(requirement);
		}
		RequirementQueryBuilder instance;
		if(requirement instanceof HasAnyAttributesRequirement) {
			instance = new HasAnyAttributesRequirementQueryBuilder();
		} else if(requirement instanceof HasAnyClassesRequirement) {
			instance = new HasAnyClassesRequirementQueryBuilder();
		} else if(requirement instanceof HasAllClassesRequirement) {
			instance = new HasAllClassesRequirementQueryBuilder();
		} else if(requirement instanceof HasStrengthRequirement) {
			instance = new HasStrengthRequirementQueryBuilder();
		} else if(requirement instanceof LightweightEdgedWeaponRequirement) {
			instance = new LightweightEdgedWeaponRequirementQueryBuilder();
		} else if(requirement instanceof CardTypeRequirement) {
			instance = new CardTypeRequirementQueryBuilder();
		} else {
			throw new RuntimeException("Unknown requirement type "+requirement.getClass());
		}
		instanceMap.put(requirement, instance);
		return instance;
	}
}

class HasAnyAttributesRequirementQueryBuilder extends RequirementQueryBuilder {
	@Override
	protected String getTableName(Requirement requirement) {
		return "Card_CardAttribute, CardAttribute";
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		int num_attributes = requirement.getValues().size();
		return "CardAttribute.attributeName in "+buildInClausePlaceholders(num_attributes)+ 
				" and Card_CardAttribute.cardTableName = ? and Card_CardAttribute.attributeId = CardAttribute._ID";
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		String mainTableName = getMainTableName(requirement);
		int num_attributes = requirement.getValues().size();
		String[] selectionArgs = new String[num_attributes+1];
		requirement.getValues().toArray(selectionArgs);
		selectionArgs[num_attributes] = mainTableName;
		return selectionArgs;
	}
}

class HasAnyClassesRequirementQueryBuilder extends RequirementQueryBuilder {
	@Override
	protected String getTableName(Requirement requirement) {
		return "Card_CardClass, CardClass";
	}

	@Override
	protected String getSelection(Requirement requirement,
			CardList currentCards) {
		int num_classes = requirement.getValues().size();
		return "CardClass.className in "+buildInClausePlaceholders(num_classes)+ 
				" and Card_CardClass.cardTableName = ? and Card_CardClass.classId = CardClass._ID";
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		String mainTableName = getMainTableName(requirement);
		int num_classes = requirement.getValues().size();
		String[] selectionArgs = new String[num_classes+1];
		requirement.getValues().toArray(selectionArgs);
		selectionArgs[num_classes] = mainTableName;
		return selectionArgs;
	}
}

class HasAllClassesRequirementQueryBuilder extends RequirementQueryBuilder {
	private final String SUBQUERY = "cardId in (SELECT cardId from Card_CardClass, CardClass where Card_CardClass.classId=CardClass._ID and CardClass.className=?)";
	@Override
	protected String getTableName(Requirement requirement) {
		return "Card_CardClass";
	}
	
	protected String buildSubQueryPlaceholders(int num_classes) {
		//length: there will be num_classes subqueries, and there will be num_classes-1 " and " strings 
		StringBuilder sb = new StringBuilder(SUBQUERY.length()*num_classes+5*(num_classes-1));
		for(int i = 0; i < num_classes; i++) {
			if(i > 0) {
				sb.append(" and ");
			}
			sb.append(SUBQUERY);
		}
		return sb.toString();
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		int num_classes = requirement.getValues().size();
		
		String selection = buildSubQueryPlaceholders(num_classes)+" and cardTableName = ?";
		return selection;
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		String mainTableName = getMainTableName(requirement);
		int num_classes = requirement.getValues().size();
		String[] selectionArgs = new String[num_classes+1];
		requirement.getValues().toArray(selectionArgs);
		selectionArgs[num_classes] = mainTableName;
		return selectionArgs;
	}
}

class HasStrengthRequirementQueryBuilder extends RequirementQueryBuilder {
	@Override
	protected String getCardIdColumn() {
		return "_ID";
	}
	
	@Override
	protected String getTableName(Requirement requirement) {
		return "HeroCard";
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		return "strength=?";
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		HasStrengthRequirement strengthRequirement = (HasStrengthRequirement)requirement;
		int strength = strengthRequirement.getStrength();
		return new String[]{Integer.toString(strength)};
	}
}

class LightweightEdgedWeaponRequirementQueryBuilder extends HasAllClassesRequirementQueryBuilder {
	@Override
	protected String getTableName(Requirement requirement) {
		return "VillageCard, Card_CardClass";
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		return "VillageCard._ID=Card_CardClass.cardId and Card_CardClass.cardTableName='VillageCard' and " +
				"VillageCard.weight <= 3 and "+buildSubQueryPlaceholders(2);
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		return new String[]{"Weapon","Edged"};
	}
}

class CardTypeRequirementQueryBuilder extends RequirementQueryBuilder {
	@Override
	protected String getCardIdColumn() {
		return "_ID";
	}
	
	@Override
	protected String getTableName(Requirement requirement) {
		return getMainTableName(requirement);
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		return null;
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		return null;
	}
}