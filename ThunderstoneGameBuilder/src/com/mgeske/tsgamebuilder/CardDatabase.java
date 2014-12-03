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
	
	public List<DungeonCard> getAllDungeonCards() {
		String tables = "ThunderstoneSet, DungeonCard " +
				"LEFT OUTER JOIN Card_CardClass ON DungeonCard._ID = Card_CardClass.cardId and Card_CardClass.cardTableName = 'DungeonCard' " +
				"LEFT OUTER JOIN CardClass ON Card_CardClass.classId = CardClass._ID " +
				"LEFT OUTER JOIN Card_CardAttribute ON DungeonCard._ID = Card_CardAttribute.cardId and Card_CardAttribute.cardTableName = 'DungeonCard' " +
				"LEFT OUTER JOIN CardAttribute ON Card_CardAttribute.attributeId = CardAttribute._ID";
		String[] columns = {"cardName", "cardType", "setName", "level", "description", "group_concat(distinct className) as classes", "group_concat(distinct attributeName) as attributes"};
		String selection = "DungeonCard.setId = ThunderstoneSet._ID";
		String groupBy = "cardName";
		CardBuilder<DungeonCard> cardBuilder = new DungeonCardBuilder();
		return getCards(cardBuilder, tables, columns, selection, groupBy);
	}
	
	public List<ThunderstoneCard> getAllThunderstoneCards() {
		String tables = "ThunderstoneSet, DungeonBossCard " +
				"LEFT OUTER JOIN Card_CardClass ON DungeonBossCard._ID = Card_CardClass.cardId and Card_CardClass.cardTableName = 'DungeonBossCard' " +
				"LEFT OUTER JOIN CardClass ON Card_CardClass.classId = CardClass._ID " +
				"LEFT OUTER JOIN Card_CardAttribute ON DungeonBossCard._ID = Card_CardAttribute.cardId and Card_CardAttribute.cardTableName = 'DungeonBossCard' " +
				"LEFT OUTER JOIN CardAttribute ON Card_CardAttribute.attributeId = CardAttribute._ID";
		String[] columns = {"cardName", "cardType", "setName", "description", "group_concat(distinct className) as classes", "group_concat(distinct attributeName) as attributes"};
		String selection = "DungeonBossCard.setId = ThunderstoneSet._ID and DungeonBossCard.cardType like 'Thunderstone%'";
		String groupBy = "cardName";
		CardBuilder<ThunderstoneCard> cardBuilder = new ThunderstoneCardBuilder();
		return getCards(cardBuilder, tables, columns, selection, groupBy);
	}
	
	public List<HeroCard> getAllHeroCards() {
		String tables = "ThunderstoneSet, HeroCard " +
				"LEFT OUTER JOIN Card_CardClass ON HeroCard._ID = Card_CardClass.cardId and Card_CardClass.cardTableName = 'HeroCard' " +
				"LEFT OUTER JOIN CardClass ON Card_CardClass.classId = CardClass._ID " +
				"LEFT OUTER JOIN Card_CardAttribute ON HeroCard._ID = Card_CardAttribute.cardId and Card_CardAttribute.cardTableName = 'HeroCard' " +
				"LEFT OUTER JOIN CardAttribute ON Card_CardAttribute.attributeId = CardAttribute._ID ";
		String[] columns = {"cardname", "setName", "description", "group_concat(distinct className) as classes", "group_concat(distinct attributeName) as attributes"};
		String selection = "HeroCard.setId = ThunderstoneSet._ID";
		String groupBy = "cardName";
		CardBuilder<HeroCard> cardBuilder = new HeroCardBuilder();
		return getCards(cardBuilder, tables, columns, selection, groupBy);
	}
	
	public List<VillageCard> getAllVillageCards() {
		String tables = "ThunderstoneSet, VillageCard " +
				"LEFT OUTER JOIN Card_CardClass ON VillageCard._ID = Card_CardClass.cardId and Card_CardClass.cardTableName = 'VillageCard' " +
				"LEFT OUTER JOIN CardClass ON Card_CardClass.classId = CardClass._ID " +
				"LEFT OUTER JOIN Card_CardAttribute ON VillageCard._ID = Card_CardAttribute.cardId and Card_CardAttribute.cardTableName = 'VillageCard' " +
				"LEFT OUTER JOIN CardAttribute on Card_CardAttribute.attributeId = CardAttribute._ID ";
		String[] columns = {"cardname", "setName", "description", "goldCost", "group_concat(distinct className) as classes", "group_concat(distinct attributeName) as attributes"};
		String selection = "VillageCard.setId = ThunderstoneSet._ID";
		String groupBy = "cardName";
		CardBuilder<VillageCard> cardBuilder = new VillageCardBuilder();
		return getCards(cardBuilder, tables, columns, selection, groupBy);
	}
	
	private <T extends Card> List<T> getCards(CardBuilder<T> cardBuilder, String tables, String[] columns, String selection, String groupBy) {
		List<T> allCards = new ArrayList<T>();
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tables);
			c = queryBuilder.query(db, columns, selection, null, groupBy, null, null, null);
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

}

abstract class CardBuilder<T extends Card> {
	public T buildCard(Cursor c, Map<String,Requirement> allRequirements) {
		String cardName = c.getString(c.getColumnIndexOrThrow("cardName"));
		String setName = c.getString(c.getColumnIndexOrThrow("setName"));
		String cardDescription = c.getString(c.getColumnIndexOrThrow("description"));
		List<String> attributes = getListFromGroupConcat(c, "attributes");
		List<String> classes = getListFromGroupConcat(c, "classes");
		List<Requirement> cardRequirements = new ArrayList<Requirement>(); 
		for(String attr : attributes) {
			//TODO use the requirements table instead of attributes
			if(attr.startsWith("REQUIRES_") || attr.startsWith("WANTS_")) {
				cardRequirements.add(allRequirements.get(attr));
			}
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

	protected abstract T buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements);
}

class DungeonCardBuilder extends CardBuilder<DungeonCard> {
	@Override
	public DungeonCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		String cardType = c.getString(c.getColumnIndexOrThrow("cardType"));
		String raw_level = c.getString(c.getColumnIndexOrThrow("level"));
		Integer level;
		if(raw_level == null) {
			level = null;
		} else {
			level = Integer.valueOf(raw_level);
		}
		return new DungeonCard(cardName, setName, cardDescription, cardType, level, attributes, classes, cardRequirements);
	}
}

class HeroCardBuilder extends CardBuilder<HeroCard> {
	@Override
	public HeroCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		return new HeroCard(cardName, setName, cardDescription, attributes, classes, cardRequirements);
	}
}

class VillageCardBuilder extends CardBuilder<VillageCard> {
	@Override
	public VillageCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		int cost = c.getInt(c.getColumnIndexOrThrow("goldCost"));
		return new VillageCard(cardName, setName, cardDescription, cost, attributes, classes, cardRequirements);
	}
}

class ThunderstoneCardBuilder extends CardBuilder<ThunderstoneCard> {
	@Override
	protected ThunderstoneCard buildCard(Cursor c, String cardName, String setName, String cardDescription, List<String> attributes, List<String> classes, List<Requirement> cardRequirements) {
		return new ThunderstoneCard(cardName, setName, cardDescription, attributes, classes, cardRequirements);
	}
}