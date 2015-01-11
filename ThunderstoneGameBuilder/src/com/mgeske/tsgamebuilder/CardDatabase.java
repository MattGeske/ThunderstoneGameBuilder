package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.Requirement;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class CardDatabase extends SQLiteAssetHelper {
	private static final String DATABASE_NAME = "cards.sqlite";
	private static final int DATABASE_VERSION = 1;
	private static Map<String,Requirement> requirementsCache = null;
	
	public CardDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public List<DungeonCard> getAllDungeonCards() {
		String[] columns = {"cardName", "cardType", "abbreviation as setName", "level", "description", "group_concat(distinct className) as classes", "group_concat(distinct attributeName) as attributes",
				"group_concat(distinct requirementName) as requirements"};
		String selection = null;
		CardBuilder<DungeonCard> cardBuilder = new DungeonCardBuilder();
		return getCards(cardBuilder, "DungeonCard", columns, selection);
	}
	
	public List<ThunderstoneCard> getAllThunderstoneCards() {
		String[] columns = {"cardName", "cardType", "abbreviation as setName",  "description", "group_concat(distinct className) as classes", "group_concat(distinct attributeName) as attributes",
				"group_concat(distinct requirementName) as requirements"};
		String selection = "DungeonBossCard.cardType like 'Thunderstone%'";
		CardBuilder<ThunderstoneCard> cardBuilder = new ThunderstoneCardBuilder();
		return getCards(cardBuilder, "DungeonBossCard", columns, selection);
	}
	
	public List<HeroCard> getAllHeroCards() {
		String[] columns = {"cardname", "abbreviation as setName", "description",  "strength", "group_concat(distinct className) as classes", "group_concat(distinct attributeName) as attributes",
				"group_concat(distinct requirementName) as requirements"};
		String selection = null;
		CardBuilder<HeroCard> cardBuilder = new HeroCardBuilder();
		return getCards(cardBuilder, "HeroCard", columns, selection);
	}
	
	public List<VillageCard> getAllVillageCards() {
		String[] columns = {"cardname", "abbreviation as setName", "description",  "goldCost", "weight", "group_concat(distinct className) as classes", "group_concat(distinct attributeName) as attributes",
				"group_concat(distinct requirementName) as requirements"};
		String selection = null;
		CardBuilder<VillageCard> cardBuilder = new VillageCardBuilder();
		return getCards(cardBuilder, "VillageCard", columns, selection);
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
	
	private String getTables(String tableName) {
		return tableName+" "+
				"LEFT OUTER JOIN Card_CardClass ON "+tableName+"._ID = Card_CardClass.cardId and Card_CardClass.cardTableName = '"+tableName+"' " +
				"LEFT OUTER JOIN CardClass ON Card_CardClass.classId = CardClass._ID " +
				"LEFT OUTER JOIN Card_CardAttribute ON "+tableName+"._ID = Card_CardAttribute.cardId and Card_CardAttribute.cardTableName = '"+tableName+"' " +
				"LEFT OUTER JOIN CardAttribute ON Card_CardAttribute.attributeId = CardAttribute._ID " +
				"LEFT OUTER JOIN Card_Requirement ON "+tableName+"._ID = Card_Requirement.cardId and Card_Requirement.cardTableName = '"+tableName+"' " +
				"LEFT OUTER JOIN Requirement ON Card_Requirement.requirementId = Requirement._ID " + 
				"LEFT OUTER JOIN Card_ThunderstoneSet ON "+tableName+"._ID = Card_ThunderstoneSet.cardId and Card_ThunderstoneSet.CardTableName = '"+tableName+"' " +
				"LEFT OUTER JOIN ThunderstoneSet ON Card_ThunderstoneSet.setId = ThunderstoneSet._ID";
	}
	
	private <T extends Card> List<T> getCards(CardBuilder<T> cardBuilder, String cardTableName, String[] columns, String selection) {
		String tables = getTables(cardTableName);
		String groupBy = "cardName";
		String having = "releaseOrder = max(releaseOrder)";
		List<T> allCards = new ArrayList<T>();
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tables);
			c = queryBuilder.query(db, columns, selection, null, groupBy, having, null, null);
			while(c.moveToNext()) {
				T card = cardBuilder.buildCard(c, getRequirements());
				allCards.add(card);
			}
			return allCards;
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

}

abstract class CardBuilder<T extends Card> {
	public T buildCard(Cursor c, Map<String,Requirement> allRequirements) {
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
	@Override
	public DungeonCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		String cardType = getString(c, "cardType");
		Integer level = getInteger(c, "level");
		return new DungeonCard(cardName, setName, cardDescription, cardType, level, attributes, classes, cardRequirements);
	}
}

class HeroCardBuilder extends CardBuilder<HeroCard> {
	@Override
	public HeroCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		int strength = getInt(c, "strength");
		return new HeroCard(cardName, setName, cardDescription, attributes, classes, cardRequirements, strength);
	}
}

class VillageCardBuilder extends CardBuilder<VillageCard> {
	@Override
	public VillageCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		int cost = getInt(c, "goldCost");
		Integer weight = getInteger(c, "weight");
		return new VillageCard(cardName, setName, cardDescription, attributes, classes, cardRequirements, cost, weight);
	}
}

class ThunderstoneCardBuilder extends CardBuilder<ThunderstoneCard> {
	@Override
	protected ThunderstoneCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		return new ThunderstoneCard(cardName, setName, cardDescription, attributes, classes, cardRequirements);
	}
}