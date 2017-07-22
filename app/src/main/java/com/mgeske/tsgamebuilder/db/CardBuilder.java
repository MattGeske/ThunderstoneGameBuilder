package com.mgeske.tsgamebuilder.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.GuardianCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;
import com.mgeske.tsgamebuilder.requirement.Requirement;

public abstract class CardBuilder {
	private SQLiteDatabase db;
	private Map<String, Requirement> allRequirements;
	private String[] chosenSets;

	protected CardBuilder(SQLiteDatabase db, Map<String,Requirement> allRequirements, String[] chosenSets) {
		this.db = db;
		this.allRequirements = allRequirements;
		this.chosenSets = chosenSets;
	}
	
	public static CardBuilder getCardBuilder(String mainTableName, SQLiteDatabase db, Map<String,Requirement> allRequirements,
				String[] chosenSets) {
		if("DungeonCard".equals(mainTableName)) {
			return new DungeonCardBuilder(db, allRequirements, chosenSets);
		} else if("DungeonBossCard".equals(mainTableName)) {
			return new ThunderstoneCardBuilder(db, allRequirements, chosenSets);
		} else if("HeroCard".equals(mainTableName)) {
			return new HeroCardBuilder(db, allRequirements, chosenSets);
		} else if("VillageCard".equals(mainTableName)) {
			return new VillageCardBuilder(db, allRequirements, chosenSets);
		} else {
			throw new RuntimeException("Couldn't determine card builder for table "+mainTableName);
		}
	}
	
	protected Card buildCard(String cardId) {
		String mainTableName = getMainTableName();
		String tables = mainTableName+", Card_ThunderstoneSet, ThunderstoneSet";
		
		List<String> columns = new ArrayList<String>();
		columns.add("cardName");
		columns.add("abbreviation");
		columns.add("setName");
		columns.add("description");
		columns.addAll(getAdditionalColumns());
		
		String selection = mainTableName+"._ID=Card_ThunderstoneSet.cardId and Card_ThunderstoneSet.cardTableName=?"+
						   " and Card_ThunderstoneSet.setId=ThunderstoneSet._ID and "+mainTableName+"._ID=?"; // and "+
		String[] selectionArgs = new String[]{mainTableName, cardId};

		String orderBy = "releaseOrder DESC";
		
		Cursor c = null;
		Card card = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tables);
			c = queryBuilder.query(db, columns.toArray(new String[]{}), selection, selectionArgs, null, null, orderBy, null);
			boolean foundSet = false;
			while(c.moveToNext()) {
				//look for the most recently released set that has been selected by the user
				String setName = c.getString(c.getColumnIndexOrThrow("setName"));
				if(arrayContains(chosenSets, setName)) {
					foundSet = true;
					break;
				}
			}
			if(!foundSet) {
				//if no sets were found that match the user's selection, use the first released set
				c.moveToLast();
			}
			card = buildCard(c, allRequirements, mainTableName, cardId);
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return card;
	}
	
	protected Card buildCard(Cursor c, Map<String,Requirement> allRequirements, String mainTableName, String cardId) {
		String cardName = getString(c, "cardName");
		String setName = getString(c, "setName");
		String setAbbreviation = getString(c, "abbreviation");
		String cardDescription = getString(c, "description");
		
		List<String> attributes = getMultipleValues("Card_CardAttribute", "CardAttribute", "attributeId", "attributeName", mainTableName, cardId);
		List<String> classes = getMultipleValues("Card_CardClass", "CardClass", "classId", "className", mainTableName, cardId);
		List<String> requirementNames = getMultipleValues("Card_Requirement","Requirement", "requirementId", "requirementName", mainTableName, cardId); 
		List<Requirement> cardRequirements = new ArrayList<Requirement>();
		for(String requirementName : requirementNames) {
			cardRequirements.add(allRequirements.get(requirementName));
		}
		return buildCard(c, cardId, cardName, setName, setAbbreviation, cardDescription, attributes, classes, cardRequirements);
	}
	
	protected List<String> getMultipleValues(String joinTableName, String tableName, String joinColumnName, String valueColumnName, String mainTableName, String cardId) {
		String[] columnNames = new String[]{valueColumnName};
		String selection = joinTableName+"."+joinColumnName+"="+tableName+"._ID AND "+
						   joinTableName+".cardTableName=? AND "+joinTableName+".cardId=?";
		String[] selectionArgs = new String[]{mainTableName, cardId};
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(joinTableName+", "+tableName);
		Cursor c = null;
		List<String> values = new ArrayList<String>();
		try {
			c = queryBuilder.query(db, columnNames, selection, selectionArgs, null, null, null);
			while(c.moveToNext()) {
				String value = c.getString(c.getColumnIndexOrThrow(valueColumnName));
				values.add(value);
			}
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return values;
	}
	
	protected abstract String getMainTableName();
	protected abstract List<String> getAdditionalColumns();
	
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
	
	protected <V extends Object> boolean arrayContains(V[] arr, V value) {
		for(V arrValue : arr) {
			if(value == arrValue || (value != null && value.equals(arrValue))) {
				return true;
			}
		}
		return false;
	}

	protected abstract Card buildCard(Cursor c, String cardId, String cardName, String setName, String setAbbreviation,
			String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements);
}

class DungeonCardBuilder extends CardBuilder {
	public DungeonCardBuilder(SQLiteDatabase db, Map<String, Requirement> allRequirements, String[] chosenSets) {
		super(db, allRequirements, chosenSets);
	}

	@Override
	public Card buildCard(Cursor c, String cardId, String cardName, String setName, String setAbbreviation, 
			String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		String cardType = getString(c, "cardType");
		Integer level = getInteger(c, "level");
		return new DungeonCard(cardId, cardName, setName, setAbbreviation, cardDescription, cardType, level, attributes, classes, cardRequirements);
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

class HeroCardBuilder extends CardBuilder {
	public HeroCardBuilder(SQLiteDatabase db, Map<String, Requirement> allRequirements, String[] chosenSets) {
		super(db, allRequirements, chosenSets);
	}

	@Override
	public Card buildCard(Cursor c, String cardId, String cardName, String setName, String setAbbreviation, 
			String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		String race = getString(c, "race");
		int strength = getInt(c, "strength");
		return new HeroCard(cardId, cardName, setName, setAbbreviation, cardDescription, attributes, classes,
				cardRequirements, race, strength);
	}

	@Override
	protected String getMainTableName() {
		return "HeroCard";
	}

	@Override
	protected List<String> getAdditionalColumns() {
		List<String> additionalColumns = new ArrayList<String>();
		additionalColumns.add("race");
		additionalColumns.add("strength");
		return additionalColumns;
	}
}

class VillageCardBuilder extends CardBuilder {
	public VillageCardBuilder(SQLiteDatabase db, Map<String, Requirement> allRequirements, String[] chosenSets) {
		super(db, allRequirements, chosenSets);
	}

	@Override
	public Card buildCard(Cursor c, String cardId, String cardName, String setName, String setAbbreviation,
			String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		int cost = getInt(c, "goldCost");
		Integer value = getInteger(c, "goldValue");
		Integer weight = getInteger(c, "weight");
		return new VillageCard(cardId, cardName, setName, setAbbreviation, cardDescription, attributes, classes,
				cardRequirements, cost, value, weight);
	}

	@Override
	protected String getMainTableName() {
		return "VillageCard";
	}

	@Override
	protected List<String> getAdditionalColumns() {
		List<String> additionalColumns = new ArrayList<String>();
		additionalColumns.add("goldCost");
		additionalColumns.add("goldValue");
		additionalColumns.add("weight");
		return additionalColumns;
	}
}

class ThunderstoneCardBuilder extends CardBuilder {
	public ThunderstoneCardBuilder(SQLiteDatabase db, Map<String, Requirement> allRequirements, String[] chosenSets) {
		super(db, allRequirements, chosenSets);
	}

	@Override
	protected Card buildCard(Cursor c, String cardId, String cardName, String setName, String setAbbreviation,
			String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		String cardType = getString(c, "cardType");
		if("Guardian".equals(cardType)) {
			return new GuardianCard(cardId, cardName, setName, setAbbreviation,
					cardDescription, attributes, classes, cardRequirements);
		} else {
			return new ThunderstoneCard(cardId, cardName, setName, setAbbreviation,
					cardDescription, cardType, attributes, classes, cardRequirements);
		}
	}

	@Override
	protected String getMainTableName() {
		return "DungeonBossCard";
	}

	@Override
	protected List<String> getAdditionalColumns() {
		List<String> additionalColumns = new ArrayList<String>();
		additionalColumns.add("cardType");
		return additionalColumns;
	}
}
