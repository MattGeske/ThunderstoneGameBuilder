package com.mgeske.tsgamebuilder.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.preference.PreferenceManager;

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
	
	private String[] getChosenSets() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String chosenSetsString = preferences.getString("chosenSets", "");
		return chosenSetsString.split(",");
	}
	
	public Iterator<? extends Card> getMatchingCards(Requirement requirement, CardList currentCards) {
		String[] chosenSets = getChosenSets();
		RequirementQueryBuilder queryBuilder = RequirementQueryBuilder.getRequirementQueryBuilder(chosenSets, requirement);
		
		SQLiteDatabase db = getReadableDatabase();
		return queryBuilder.queryMatchingCards(currentCards, db, getRequirements());
	}

	public String[] getSavedGames() {
		String[] columns = {"gameName"};
		String tables = "SavedGame";
		String sortOrder = "gameName ASC";
		
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tables);
			c = queryBuilder.query(db, columns, null, null, null, null, sortOrder);
			int count = c.getCount();
			String[] gameNames = new String[count];
			int gameNameIndex = c.getColumnIndexOrThrow("gameName");
			for(int i = 0; i < count; i++) {
				c.moveToNext();
				String gameName = c.getString(gameNameIndex);
				gameNames[i] = gameName;
			}
			return gameNames;
		} finally {
			if(c != null) {
				c.close();
			}
		}
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
				CardBuilder<? extends Card> cardBuilder = CardBuilder.getCardBuilder(cardTableName, db, getRequirements(), getChosenSets());
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

