package com.mgeske.tsgamebuilder.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;
import com.mgeske.tsgamebuilder.requirement.Requirement;

public abstract class CardBuilder<T extends Card> {
	private SQLiteDatabase db;
	private Map<String, Requirement> allRequirements;

	protected CardBuilder(SQLiteDatabase db, Map<String,Requirement> allRequirements) {
		this.db = db;
		this.allRequirements = allRequirements;
	}
	
	public static CardBuilder<? extends Card> getCardBuilder(String mainTableName, SQLiteDatabase db, Map<String,Requirement> allRequirements) {
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
	
	protected T buildCard(String cardId) {
		String mainTableName = getMainTableName();
		String tables = mainTableName+", Card_ThunderstoneSet, ThunderstoneSet";
		
		List<String> columns = new ArrayList<String>();
		columns.add("cardName");
		columns.add("abbreviation as setName");
		columns.add("description");
		columns.addAll(getAdditionalColumns());
		
		String selection = mainTableName+"._ID=Card_ThunderstoneSet.cardId and Card_ThunderstoneSet.cardTableName=?"+
						   " and Card_ThunderstoneSet.setId=ThunderstoneSet._ID and "+mainTableName+"._ID=?";
		String[] selectionArgs = new String[]{mainTableName, cardId};
		String groupBy = "cardName";
		String having = "releaseOrder = max(releaseOrder)";
		
		Cursor c = null;
		T card = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tables);
			c = queryBuilder.query(db, columns.toArray(new String[]{}), selection, selectionArgs, groupBy, having, null, null);
			c.moveToNext();
			card = buildCard(c, allRequirements, mainTableName, cardId);
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return card;
	}
	
	protected T buildCard(Cursor c, Map<String,Requirement> allRequirements, String mainTableName, String cardId) {
		String cardName = getString(c, "cardName");
		String setName = getString(c, "setName");
		String cardDescription = getString(c, "description");
		
		List<String> attributes = getMultipleValues("Card_CardAttribute", "CardAttribute", "attributeId", "attributeName", mainTableName, cardId);
		List<String> classes = getMultipleValues("Card_CardClass", "CardClass", "classId", "className", mainTableName, cardId);
		List<String> requirementNames = getMultipleValues("Card_Requirement","Requirement", "requirementId", "requirementName", mainTableName, cardId); 
		List<Requirement> cardRequirements = new ArrayList<Requirement>();
		for(String requirementName : requirementNames) {
			cardRequirements.add(allRequirements.get(requirementName));
		}
		return buildCard(c, cardId, cardName, setName, cardDescription, attributes, classes, cardRequirements);
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

	protected abstract T buildCard(Cursor c, String cardId, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements);
}

class DungeonCardBuilder extends CardBuilder<DungeonCard> {
	public DungeonCardBuilder(SQLiteDatabase db, Map<String, Requirement> allRequirements) {
		super(db, allRequirements);
	}

	@Override
	public DungeonCard buildCard(Cursor c, String cardId, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		String cardType = getString(c, "cardType");
		Integer level = getInteger(c, "level");
		return new DungeonCard(cardId, cardName, setName, cardDescription, cardType, level, attributes, classes, cardRequirements);
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
	public HeroCard buildCard(Cursor c, String cardId, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		int strength = getInt(c, "strength");
		return new HeroCard(cardId, cardName, setName, cardDescription, attributes, classes, cardRequirements, strength);
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
	public VillageCard buildCard(Cursor c, String cardId, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		int cost = getInt(c, "goldCost");
		Integer weight = getInteger(c, "weight");
		return new VillageCard(cardId, cardName, setName, cardDescription, attributes, classes, cardRequirements, cost, weight);
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
	protected ThunderstoneCard buildCard(Cursor c, String cardId, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		return new ThunderstoneCard(cardId, cardName, setName, cardDescription, attributes, classes, cardRequirements);
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
