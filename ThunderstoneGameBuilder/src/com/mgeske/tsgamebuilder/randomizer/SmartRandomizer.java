package com.mgeske.tsgamebuilder.randomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
			//after choosing cards, these lists will contain all cards not yet considered by the randomizer
			List<DungeonCard> remainingDungeonCards = new ArrayList<DungeonCard>(allDungeonCards);
			List<ThunderstoneCard> remainingThunderstoneCards = new ArrayList<ThunderstoneCard>(allThunderstoneCards);
			List<HeroCard> remainingHeroCards = new ArrayList<HeroCard>(allHeroCards);
			List<VillageCard> remainingVillageCards = new ArrayList<VillageCard>(allVillageCards);
			
			List<DungeonCard> dungeonList = chooseDungeonCards(remainingDungeonCards);
			List<ThunderstoneCard> thunderstoneList = chooseThunderstoneCards(remainingThunderstoneCards);
			
			@SuppressWarnings("unchecked")
			Set<String> attributesRequiredByMonsters = getRequiredAttributes(dungeonList, thunderstoneList);
			
			Set<String> requiredHeroAttributes = getHeroRequirements(attributesRequiredByMonsters);
			logger.info("Required attributes for heroes: "+requiredHeroAttributes);
			
			List<HeroCard> heroList = chooseHeroCards(remainingHeroCards, requiredHeroAttributes);
			

			@SuppressWarnings("unchecked")
			Set<String> attributesRequiredByHeroes = getRequiredAttributes(heroList);
			Set<String> requiredVillageAttributes = getVillageRequirements(attributesRequiredByMonsters, attributesRequiredByHeroes, heroList);
			logger.info("Required attributes for village: "+requiredVillageAttributes);
			
			List<VillageCard> villageList = chooseVillageCards(remainingVillageCards, requiredVillageAttributes);
	
			logger.info("Finished generating card set.");
			CardList cardList = new CardList(dungeonList, thunderstoneList, heroList, villageList);
			return cardList;
		} finally {
			if(cardDb != null) {
				cardDb.close();
			}
		}
	}

	private Set<String> getHeroRequirements(Set<String> attributesRequiredByMonsters) {
		Set<String> heroRequirements = new HashSet<String>();
		for(String attribute : attributesRequiredByMonsters) {
			//TODO implement this better
			if(attribute.equals("HAS_MAGIC_ATTACK") || attribute.equals("HAS_PHYSICAL_ATTACK") || attribute.equals("REMOVES_DISEASE")) {
				heroRequirements.add(attribute);
			}
		}
		return heroRequirements;
	}
	
	private Set<String> getVillageRequirements(Set<String> attributesRequiredByMonsters, Set<String> attributesRequiredByHeroes, List<HeroCard> heroList) {
		Set<String> villageRequirements = new HashSet<String>();
		villageRequirements.addAll(attributesRequiredByMonsters);
		villageRequirements.addAll(attributesRequiredByHeroes);
		for(HeroCard card : heroList) {
			villageRequirements.removeAll(card.getAttributes());
		}
		return villageRequirements;
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
	
	private List<DungeonCard> chooseDungeonCards(List<DungeonCard> remainingDungeonCards) {
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
		
		return chooseCards(remainingDungeonCards, minimumNumOfCards, maximumNumOfCards, null);
	}

	private List<ThunderstoneCard> chooseThunderstoneCards(List<ThunderstoneCard> remainingThunderstoneCards) {
		//TODO for now, assumes you want 1 thunderstone
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Thunderstone", 1);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Thunderstone", 1);
		
		return chooseCards(remainingThunderstoneCards, minimumNumOfCards, maximumNumOfCards, null);
	}

	private List<HeroCard> chooseHeroCards(List<HeroCard> remainingHeroCards, Set<String> requiredAttributes) {
		//TODO for now, assumes you want 4 heroes
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Hero", 4);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Hero", 4);
		
		return chooseCards(remainingHeroCards, minimumNumOfCards, maximumNumOfCards, requiredAttributes);
	}

	private List<VillageCard> chooseVillageCards(List<VillageCard> remainingVillageCards, Set<String> requiredAttributes) {
		//TODO for now, assumes you want 8 total village cards with no more than 3 weapons, 2 items, 3 spells, and 3 villagers (TS Advance rules)
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Village", 8);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Village", 8);
		maximumNumOfCards.put("Weapon", 3);
		maximumNumOfCards.put("Item", 2);
		maximumNumOfCards.put("Spell", 3);
		maximumNumOfCards.put("Villager", 3);
		
		return chooseCards(remainingVillageCards, minimumNumOfCards, maximumNumOfCards, requiredAttributes);
	}
	
	private <T extends Card> List<T> chooseCards(List<T> allCards, Map<String,Integer> minimumNumOfCards, Map<String,Integer> maximumNumOfCards, Set<String> requiredAttributes) {
		List<T> localAllCards = new ArrayList<T>();
		localAllCards.addAll(allCards);
		Map<String,Integer> localMinNumOfCards = new HashMap<String,Integer>();
		localMinNumOfCards.putAll(minimumNumOfCards);
		Map<String,Integer> localMaxNumOfCards = new HashMap<String,Integer>();
		localMaxNumOfCards.putAll(maximumNumOfCards);
		Set<String> remainingRequiredAttributes = new HashSet<String>();
		if(requiredAttributes != null) {
			remainingRequiredAttributes.addAll(requiredAttributes);
		}
		
		List<T> chosenCards = new ArrayList<T>();
		Random r = new Random();
		while(!metAllRequirements(localMinNumOfCards) && localAllCards.size() > 0) {
			int index = r.nextInt(localAllCards.size());
			T card = localAllCards.get(index);
			List<String> keys = card.getRandomizerKeys();
			boolean can_use_card = true;
			Map<String,Integer> maxUpdates = new HashMap<String,Integer>();
			Map<String,Integer> minUpdates = new HashMap<String,Integer>();
			if(remainingRequiredAttributes.size() > 0 && !containsAny(remainingRequiredAttributes, card.getAttributes())) {
				can_use_card = false;
				logger.info("Discarding card "+card.getCardName()+" because it doesn't fulfill any requirements.");
			} else {
				for(String key : keys) {
					Integer remainingMaxCards = localMaxNumOfCards.get(key);
					if(remainingMaxCards != null && remainingMaxCards <= 0) {
						can_use_card = false;
						logger.info("Discarding card "+card.getCardName()+" because I already found enough "+key);
						break;
					}
					if(remainingMaxCards != null) { //if not null, guaranteed to be >0
						maxUpdates.put(key, remainingMaxCards-1);
					}
					Integer remainingMinCards = localMinNumOfCards.get(key);
					if(remainingMinCards != null && remainingMinCards > 0) {
						minUpdates.put(key, remainingMinCards-1);
					}
				}
			}
			if(can_use_card) {
				chosenCards.add(card);
				remainingRequiredAttributes.removeAll(card.getAttributes());
				localMaxNumOfCards.putAll(maxUpdates);
				localMinNumOfCards.putAll(minUpdates);
			}
			localAllCards.remove(index);
		}
		if(!metAllRequirements(localMinNumOfCards)) {
			throw new RuntimeException("Couldn't choose cards meeting the requirements "+minimumNumOfCards+". Remaining requirements: "+localMinNumOfCards);
		}
		if(remainingRequiredAttributes.size() > 0) {
			throw new RuntimeException("Couldn't choose cards meeting the requirements "+remainingRequiredAttributes+".");
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
	
	private Set<String> getRequiredAttributes(List<? extends Card>... chosenCards) {
		Set<String> requiredAttributes = new HashSet<String>();
		for(List<? extends Card> cardList : chosenCards) {
			for(Card card : cardList) {
				for(String attribute : card.getAttributes()) {
					if(attribute.startsWith("REQUIRES_")) {
						attribute = attribute.replace("REQUIRES_", "HAS_");
						requiredAttributes.add(attribute);
					} else if(attribute.equals("GIVES_DISEASE")) {
						//special case since we might later want to add a requirement for GIVES_DISEASE when we have a card with REMOVES_DISEASE
						requiredAttributes.add("REMOVES_DISEASE");
					}
				}
			}
		}
		return requiredAttributes;
	}
	
	private boolean containsAny(Set<String> requiredAttributes, List<String> cardAttributes) {
		for(String attribute : cardAttributes) {
			if(requiredAttributes.contains(attribute)) {
				return true;
			}
		}
		return false;
	}
}
