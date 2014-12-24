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
import com.mgeske.tsgamebuilder.card.Requirement;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;

public class SmartRandomizer implements IRandomizer {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private List<DungeonCard> allDungeonCards = null;
	private List<ThunderstoneCard> allThunderstoneCards = null;
	private List<HeroCard> allHeroCards = null;
	private List<VillageCard> allVillageCards = null;
	
	private int num_monster;
	private int num_thunderstone;
	private int num_hero;
	private int num_village;
	
	public SmartRandomizer(int num_monster, int num_thunderstone, int num_hero, int num_village) {
		setLimits(num_monster, num_thunderstone, num_hero, num_village);
	}

	public void setLimits(int num_monster, int num_thunderstone, int num_hero, int num_village) {
		this.num_monster = num_monster;
		this.num_thunderstone = num_thunderstone;
		this.num_hero = num_hero;
		this.num_village = num_village;
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
			
			Set<Requirement> requiredHeroAttributes = getHeroRequirements(dungeonList, thunderstoneList);
			logger.info("Required attributes for heroes: "+requiredHeroAttributes);
			
			List<HeroCard> heroList = chooseHeroCards(remainingHeroCards, requiredHeroAttributes);
			

			Set<Requirement> requiredVillageAttributes = getVillageRequirements(dungeonList, thunderstoneList, heroList);
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

	private Set<Requirement> getHeroRequirements(List<DungeonCard> dungeonList, List<ThunderstoneCard> thunderstoneList) {
		Set<Requirement> heroRequirements = new HashSet<Requirement>();
		addRequirementsForType(heroRequirements, dungeonList, "Hero");
		addRequirementsForType(heroRequirements, thunderstoneList, "Hero");
		return heroRequirements;
	}
	
	private Set<Requirement> getVillageRequirements(List<DungeonCard> dungeonList, List<ThunderstoneCard> thunderstoneList, List<HeroCard> heroList) {
		Set<Requirement> villageRequirements = new HashSet<Requirement>();
		addRequirementsForType(villageRequirements, dungeonList, "Village");
		addRequirementsForType(villageRequirements, thunderstoneList, "Village");
		addRequirementsForType(villageRequirements, heroList, "Village");
		return villageRequirements;
	}
	
	private void addRequirementsForType(Set<Requirement> requirements, List<? extends Card> cardList, String cardType) {
		for(Card card : cardList) {
			for(Requirement req : card.getRequirements()) {
				if(req.getRequiredOn().equals(cardType)) {
					requirements.add(req);
				}
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
	
	private List<DungeonCard> chooseDungeonCards(List<DungeonCard> remainingDungeonCards) {
		//TODO for now, assumes you want a monster of each level
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Monster", num_monster);
		if(num_monster == 3) {
			//only enforce the minimum if there are enough cards to do so
			//TODO ideally this should be when num_monster >= 3, but that introduces some difficulties that will require significant refactoring
			//     to make the minimums behave more like requirements
			minimumNumOfCards.put("Level1Monster", 1);
			minimumNumOfCards.put("Level2Monster", 1);
			minimumNumOfCards.put("Level3Monster", 1);
		}
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Monster", num_monster);
		if(num_monster <= 3) {
			//only enforce the maximum if there are few enough cards to do so
			maximumNumOfCards.put("Level1Monster", 1);
			maximumNumOfCards.put("Level2Monster", 1);
			maximumNumOfCards.put("Level3Monster", 1);
		}
		maximumNumOfCards.put("Treasure", 1);
		maximumNumOfCards.put("Trap", 1);
		
		return chooseCards(remainingDungeonCards, minimumNumOfCards, maximumNumOfCards, null);
	}

	private List<ThunderstoneCard> chooseThunderstoneCards(List<ThunderstoneCard> remainingThunderstoneCards) {
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Thunderstone", num_thunderstone);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Thunderstone", num_thunderstone);
		
		return chooseCards(remainingThunderstoneCards, minimumNumOfCards, maximumNumOfCards, null);
	}

	private List<HeroCard> chooseHeroCards(List<HeroCard> remainingHeroCards, Set<Requirement> requiredAttributes) {
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Hero", num_hero);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Hero", num_hero);
		return chooseCards(remainingHeroCards, minimumNumOfCards, maximumNumOfCards, requiredAttributes);
		
	}

	private List<VillageCard> chooseVillageCards(List<VillageCard> remainingVillageCards, Set<Requirement> requiredAttributes) {
		//TODO for now, assumes you want no more than 3 weapons, 2 items, 3 spells, and 3 villagers (TS Advance rules)
		Map<String,Integer> minimumNumOfCards = new HashMap<String,Integer>();
		minimumNumOfCards.put("Village", num_village);
		Map<String,Integer> maximumNumOfCards = new HashMap<String,Integer>();
		maximumNumOfCards.put("Village", num_village);
		if(num_village <= 11) {
			//only enforce the maximum if there are few enough cards to do so
			maximumNumOfCards.put("Weapon", 3);
			maximumNumOfCards.put("Item", 2);
			maximumNumOfCards.put("Spell", 3);
			maximumNumOfCards.put("Villager", 3);
		}
		
		return chooseCards(remainingVillageCards, minimumNumOfCards, maximumNumOfCards, requiredAttributes);
	}
	
	private <T extends Card> List<T> chooseCards(List<T> allCards, Map<String,Integer> minimumNumOfCards, Map<String,Integer> maximumNumOfCards, Set<Requirement> requiredAttributes) {
		//make local copies of the collections so we can modify them without breaking callers
		List<T> localAllCards = new ArrayList<T>(allCards);
		Map<String,Integer> localMinNumOfCards = new HashMap<String,Integer>(minimumNumOfCards);
		Map<String,Integer> localMaxNumOfCards = new HashMap<String,Integer>(maximumNumOfCards);
		Set<Requirement> remainingRequirements = new HashSet<Requirement>();
		if(requiredAttributes != null) {
			remainingRequirements.addAll(requiredAttributes);
		}
		
		List<T> chosenCards = new ArrayList<T>();
		Random r = new Random();
		while(!metMinimumCardRequirements(localMinNumOfCards) && localAllCards.size() > 0) {
			int index = r.nextInt(localAllCards.size());
			T card = localAllCards.get(index);
			List<String> keys = card.getRandomizerKeys();
			boolean can_use_card = true;
			Map<String,Integer> maxUpdates = new HashMap<String,Integer>();
			Map<String,Integer> minUpdates = new HashMap<String,Integer>();
			Set<Requirement> matchedRequirements = getMatchingRequirements(remainingRequirements, card);
			if(remainingRequirements.size() > 0 && matchedRequirements.size() == 0) {
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
				if(matchedRequirements.size() > 0) {
					logger.info("Card "+card.getCardName()+" fulfills requirements "+matchedRequirements);
				}
				remainingRequirements.removeAll(matchedRequirements);
				localMaxNumOfCards.putAll(maxUpdates);
				localMinNumOfCards.putAll(minUpdates);
			}
			localAllCards.remove(index);
		}
		if(!metMinimumCardRequirements(localMinNumOfCards)) {
			throw new RuntimeException("Couldn't choose cards meeting the requirements "+minimumNumOfCards+". Remaining requirements: "+localMinNumOfCards);
		}
		if(remainingRequirements.size() > 0) {
			throw new RuntimeException("Couldn't choose cards meeting the requirements "+remainingRequirements+".");
		}
		Collections.sort(chosenCards);
		
		return chosenCards;
	}
	
	private boolean metMinimumCardRequirements(Map<String,Integer> minimumNumOfCards) {
		for(Integer i : minimumNumOfCards.values()) {
			if(i > 0) {
				return false;
			}
		}
		return true;
	}
	
	private Set<Requirement> getMatchingRequirements(Set<Requirement> remainingRequirements, Card card) {
		Set<Requirement> matchingRequirements = new HashSet<Requirement>();
		for(Requirement req : remainingRequirements) {
			if(req.match(card)) {
				matchingRequirements.add(req);
			}
		}
		return matchingRequirements;
	}
}
