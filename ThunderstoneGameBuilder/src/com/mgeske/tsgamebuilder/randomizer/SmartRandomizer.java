package com.mgeske.tsgamebuilder.randomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import android.content.Context;

import com.mgeske.tsgamebuilder.CardDatabase;
import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;

public class SmartRandomizer implements IRandomizer {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private List<DungeonCard> allDungeonCards = null;
	private List<ThunderstoneCard> allThunderstoneCards = null;
	private List<HeroCard> allHeroCards = null;
	private List<VillageCard> allVillageCards = null;

	public SmartRandomizer() {
	}

	@Override
	public CardList generateCardList(Context context) {
		if(allDungeonCards == null || allThunderstoneCards == null || allHeroCards == null || allVillageCards == null) {
			populateCardLists(context);
		}
		CardDatabase cardDb = null;
		try {
			cardDb = new CardDatabase(context);
			//first select some dungeon cards
			List<DungeonCard> dungeonList = chooseDungeonCards();
			
			List<ThunderstoneCard> thunderstoneList = chooseThunderstoneCards();
			
			List<HeroCard> heroList = chooseHeroCards();
			
			List<VillageCard> villageList = chooseVillageCards();
	
			CardList cardList = new CardList(dungeonList, thunderstoneList, heroList, villageList);
			return cardList;
		} finally {
			if(cardDb != null) {
				cardDb.close();
			}
		}
	}
	
	private void populateCardLists(Context context) {
		CardDatabase cardDb = null;
		try {
			cardDb = new CardDatabase(context);
			//first select some dungeon cards
			allDungeonCards = cardDb.getAllDungeonCards();
			allThunderstoneCards = cardDb.getAllThunderstoneCards();
			allHeroCards = cardDb.getAllHeroCards();
			allVillageCards = cardDb.getAllVillageCards();
		} finally {
			if(cardDb != null) {
				cardDb.close();
			}
		}
	}
	
	private List<DungeonCard> chooseDungeonCards() {
		//TODO for now, assumes you want a monster of each level
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Level1Monster", 1);
		minimumNumOfCards.put("Level2Monster", 1);
		minimumNumOfCards.put("Level3Monster", 1);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Level1Monster", 1);
		maximumNumOfCards.put("Level2Monster", 1);
		maximumNumOfCards.put("Level3Monster", 1);
		maximumNumOfCards.put("Treasure", 1);
		maximumNumOfCards.put("Trap", 1);
		
		return chooseCards(allDungeonCards, minimumNumOfCards, maximumNumOfCards);
	}

	private List<ThunderstoneCard> chooseThunderstoneCards() {
		//TODO for now, assumes you want 1 thunderstone
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Thunderstone", 1);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Thunderstone", 1);
		
		return chooseCards(allThunderstoneCards, minimumNumOfCards, maximumNumOfCards);
	}

	private List<HeroCard> chooseHeroCards() {
		//TODO for now, assumes you want 4 heroes
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Hero", 4);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Hero", 4);
		
		return chooseCards(allHeroCards, minimumNumOfCards, maximumNumOfCards);
	}

	private List<VillageCard> chooseVillageCards() {
		//TODO for now, assumes you want 8 total village cards
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Village", 8);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Village", 8);
		
		return chooseCards(allVillageCards, minimumNumOfCards, maximumNumOfCards);
	}
	
	private <T extends Card> List<T> chooseCards(List<T> allCards, Map<String,Integer> minimumNumOfCards, Map<String,Integer> maximumNumOfCards) {
		List<T> localAllCards = new ArrayList<T>();
		localAllCards.addAll(allCards);
		Map<String,Integer> localMinNumOfCards = new HashMap<String,Integer>();
		localMinNumOfCards.putAll(minimumNumOfCards);
		Map<String,Integer> localMaxNumOfCards = new HashMap<String,Integer>();
		localMaxNumOfCards.putAll(maximumNumOfCards);
		
		List<T> chosenCards = new ArrayList<T>();
		Random r = new Random();
		while(!metAllRequirements(localMinNumOfCards) && localAllCards.size() > 0) {
			int index = r.nextInt(localAllCards.size());
			T card = localAllCards.get(index);
			String key = card.getRandomizerKey();
			Integer remainingMaxCards = localMaxNumOfCards.get(key);
			if(remainingMaxCards != null && remainingMaxCards > 0) {
				chosenCards.add(card);
				localMaxNumOfCards.put(key, remainingMaxCards-1);
				Integer remainingMinCards = localMinNumOfCards.get(key);
				if(remainingMinCards != null && remainingMinCards > 0) {
					localMinNumOfCards.put(key, remainingMinCards-1);
				}
			} else {
				logger.info("Discarding card "+card.getCardName()+" because I already found enough "+key);
			}
			localAllCards.remove(index);
		}
		if(!metAllRequirements(localMinNumOfCards)) {
			throw new RuntimeException("Couldn't choose cards meeting the requirements "+minimumNumOfCards+". Remaining requirements: "+localMinNumOfCards);
		}
		Collections.sort(chosenCards);
		
		return chosenCards;
	}
	
	private boolean metAllRequirements(Map<String,Integer> minimumNumOfCards) {
		for(Integer i : minimumNumOfCards.values()) {
			if(i > 0) {
				return false;
			}
		}
		return true;
	}
}
