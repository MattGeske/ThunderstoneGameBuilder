package com.mgeske.tsgamebuilder.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mgeske.tsgamebuilder.SavedGame;
import com.mgeske.tsgamebuilder.Util;
import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.requirement.Requirement;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class CardDatabase extends SQLiteAssetHelper {
	private static final String DATABASE_NAME = "cards.sqlite";
	private static final int DATABASE_VERSION = 1;
	private static Map<String,Requirement> requirementsCache = null;
	private Context context;
	
	public CardDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	protected static String buildInClausePlaceholders(String columnName, int num_values, boolean invert) {
		StringBuilder sb = new StringBuilder();
		sb.append(columnName);
		if(invert) {
			sb.append(" not");
		}
		sb.append(" in (");
		for(int i = 0; i < num_values; i++) {
			if(i > 0) {
				sb.append(",");
			}
			sb.append("?");
		}
		sb.append(")");
		return sb.toString();
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
		String[] chosenSets = Util.getChosenSets(context);
		RequirementQueryBuilder queryBuilder = RequirementQueryBuilder.getRequirementQueryBuilder(chosenSets, requirement);
		
		SQLiteDatabase db = getReadableDatabase();
		return queryBuilder.queryMatchingCards(currentCards, db, getRequirements());
	}

	public List<SavedGame> getSavedGames() {
		String[] columns = {"_ID", "gameName"};
		String tables = "SavedGame";
		String orderBy = "gameName ASC";
		
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tables);
			c = queryBuilder.query(db, columns, null, null, null, null, orderBy);
			List<SavedGame> gameInfo = new ArrayList<SavedGame>();
			int gameNameIndex = c.getColumnIndexOrThrow("gameName");
			int gameIdIndex = c.getColumnIndexOrThrow("_ID");
			while(c.moveToNext()) {
				String gameName = c.getString(gameNameIndex);
				String gameId = c.getString(gameIdIndex);
				String[] setNames = getSetNamesForSavedGame(gameId);
				SavedGame savedGame = new SavedGame(gameName, setNames);
				gameInfo.add(savedGame);
			}
			return gameInfo;
		} finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	private String[] getSetNamesForSavedGame(String savedGameId) {
		String sql = "SELECT group_concat(setName) as setNames " +
					 "FROM ( " +
					 "    SELECT Card_SavedGame.cardId, setName " +
					 "    FROM Card_SavedGame, Card_ThunderstoneSet, ThunderstoneSet " +
					 "    WHERE Card_SavedGame.cardId=Card_ThunderstoneSet.cardId AND " +
					 "    Card_SavedGame.cardTableName=Card_ThunderstoneSet.cardTableName AND " +
					 "    Card_ThunderstoneSet.setId=ThunderstoneSet._ID AND " +
					 "    Card_SavedGame.savedGameId=? " +
					 "    ORDER BY Card_SavedGame.cardId, releaseOrder ASC " +
					 ") " +
					 "GROUP BY cardId";
		Set<String> foundSets = new HashSet<String>();
		String[] chosenSets = Util.getChosenSets(context);
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = getReadableDatabase();
			c = db.rawQuery(sql, new String[]{savedGameId});
			while(c.moveToNext()) {
				String setNamesString = c.getString(c.getColumnIndexOrThrow("setNames"));
				String[] setNames = setNamesString.split(",");
				boolean foundSet = false;
				for(String setName : setNames) {
					if(Util.arrayContains(chosenSets, setName)) {
						foundSets.add(setName);
						foundSet = true;
						break;
					}
				}
				if(!foundSet) {
					foundSets.add(setNames[0]);
				}
			}
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return foundSets.toArray(new String[foundSets.size()]);
	}
	
	public CardList loadSavedGame(String gameName) {
		Map<String,Integer> emptyMap = Collections.emptyMap();
		CardList cardList = new CardList(emptyMap, emptyMap);
		
		String[] columns = {"cardId", "cardTableName"};
		String tables = "Card_SavedGame, SavedGame";
		String selection = "Card_SavedGame.savedGameId=SavedGame._ID and SavedGame.gameName=?";
		String[] selectionArgs = new String[]{gameName};
		
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tables);
			c = queryBuilder.query(db, columns, selection, selectionArgs, null, null, null);
			int cardIdIndex = c.getColumnIndexOrThrow("cardId");
			int cardTableNameIndex = c.getColumnIndexOrThrow("cardTableName");
			while(c.moveToNext()) {
				String cardId = c.getString(cardIdIndex);
				String cardTableName = c.getString(cardTableNameIndex);
				CardBuilder cardBuilder = CardBuilder.getCardBuilder(cardTableName, db, getRequirements(), Util.getChosenSets(context));
				Card card = cardBuilder.buildCard(cardId);
				cardList.addCard(card);
			}
			return cardList;
		} finally {
			if(c != null) {
				c.close();
			}
		}
	}
}

