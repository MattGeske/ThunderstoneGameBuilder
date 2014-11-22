package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class CardDatabase extends SQLiteAssetHelper {
	private static final String DATABASE_NAME = "cards.sqlite";
	private static final int DATABASE_VERSION = 1;
	
	public CardDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public List<DungeonCard> getAllDungeonCards() {
		String tables = "DungeonCard, ThunderstoneSet";
		String[] columns = {"DungeonCard.cardName", "DungeonCard.cardType", "ThunderstoneSet.setName", "DungeonCard.level", "DungeonCard.description"};
		String selection = "DungeonCard.setId = ThunderstoneSet._ID";
		CardBuilder<DungeonCard> cardBuilder = new DungeonCardBuilder();
		return getCards(cardBuilder, tables, columns, selection);
	}
	
	public List<ThunderstoneCard> getAllThunderstoneCards() {
		String tables = "DungeonBossCard, ThunderstoneSet";
		String[] columns = {"DungeonBossCard.cardName", "DungeonBossCard.cardType", "ThunderstoneSet.setName", "DungeonBossCard.description"};
		String selection = "DungeonBossCard.setId = ThunderstoneSet._ID and DungeonBossCard.cardType like 'Thunderstone%'";
		CardBuilder<ThunderstoneCard> cardBuilder = new ThunderstoneCardBuilder();
		return getCards(cardBuilder, tables, columns, selection);
	}
	
	public List<HeroCard> getAllHeroCards() {
		String tables = "HeroCard, ThunderstoneSet";
		String[] columns = {"HeroCard.cardname", "ThunderstoneSet.setName", "HeroCard.description"};
		String selection = "HeroCard.setId = ThunderstoneSet._ID";
		CardBuilder<HeroCard> cardBuilder = new HeroCardBuilder();
		return getCards(cardBuilder, tables, columns, selection);
	}
	
	public List<VillageCard> getAllVillageCards() {
		String tables = "VillageCard, ThunderstoneSet";
		String[] columns = {"VillageCard.cardname", "ThunderstoneSet.setName", "VillageCard.description", "VillageCard.goldCost"};
		String selection = "VillageCard.setId = ThunderstoneSet._ID";
		CardBuilder<VillageCard> cardBuilder = new VillageCardBuilder();
		return getCards(cardBuilder, tables, columns, selection);
	}
	
	private <T extends Card> List<T> getCards(CardBuilder<T> cardBuilder, String tables, String[] columns, String selection) {
		List<T> allCards = new ArrayList<T>();
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tables);
			c = queryBuilder.query(db, columns, selection, null, null, null, null, null);
			while(c.moveToNext()) {
				T card = cardBuilder.buildCard(c);
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
	public T buildCard(Cursor c) {
		String cardName = c.getString(c.getColumnIndexOrThrow("cardName"));
		String setName = c.getString(c.getColumnIndexOrThrow("setName"));
		String cardDescription = c.getString(c.getColumnIndexOrThrow("description"));
		return buildCard(c, cardName, setName, cardDescription);
	}

	protected abstract T buildCard(Cursor c, String cardName, String setName, String cardDescription);
}

class DungeonCardBuilder extends CardBuilder<DungeonCard> {
	@Override
	public DungeonCard buildCard(Cursor c, String cardName, String setName, String cardDescription) {
		String cardType = c.getString(c.getColumnIndexOrThrow("cardType"));
		String raw_level = c.getString(c.getColumnIndexOrThrow("level"));
		Integer level;
		if(raw_level == null) {
			level = null;
		} else {
			level = Integer.valueOf(raw_level);
		}
		return new DungeonCard(cardName, setName, cardDescription, cardType, level);
	}
}

class HeroCardBuilder extends CardBuilder<HeroCard> {
	@Override
	public HeroCard buildCard(Cursor c, String cardName, String setName, String cardDescription) {
		return new HeroCard(cardName, setName, cardDescription);
	}
}

class VillageCardBuilder extends CardBuilder<VillageCard> {
	@Override
	public VillageCard buildCard(Cursor c, String cardName, String setName, String cardDescription) {
		int cost = c.getInt(c.getColumnIndexOrThrow("goldCost"));
		return new VillageCard(cardName, setName, cardDescription, cost);
	}
}

class ThunderstoneCardBuilder extends CardBuilder<ThunderstoneCard> {
	@Override
	protected ThunderstoneCard buildCard(Cursor c, String cardName, String setName, String cardDescription) {
		return new ThunderstoneCard(cardName, setName, cardDescription);
	}
}