package com.mgeske.tsgamebuilder.randomizer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.db.CardDatabase;
import com.mgeske.tsgamebuilder.requirement.Requirement;

public class SmartRandomizer implements IRandomizer {
	private CardDatabase cardDb;
	private Random random;
	
	public SmartRandomizer(CardDatabase cardDb) {
		this.cardDb = cardDb;
		this.random = new Random();
	}
	
	@Override
	public CardList generateCardList(int num_monster, int num_thunderstone, int num_hero, int num_village, boolean village_limits, boolean monster_levels) {
		//set up min/max card requirements
		Map<String,Integer> minimumCards = getMinimumCards(num_monster, num_thunderstone, num_hero, num_village, monster_levels);
		Map<String,Integer> maximumCards = getMaximumCards(num_monster, num_thunderstone, num_hero, num_village, village_limits, monster_levels);
		
		CardList cardList = new CardList(minimumCards, maximumCards);
		while(cardList.hasRemainingMinimums()) {
			Requirement randomRequirement = getRandomCardRequirement(cardList.getRemainingMinimums());
			if(!chooseCards(randomRequirement, cardList)) {
				throw new RuntimeException("Couldn't choose cards! Remaining minimums: "+cardList.getRemainingMinimums());
			}
		}
		return cardList;
	}
	
	private Map<String,Integer> getMinimumCards(int num_monster, int num_thunderstone, int num_hero, int num_village, boolean monster_levels) {
		Map<String,Integer> minimumCards = new HashMap<String,Integer>();
		minimumCards.put("Monster", num_monster);
		if(monster_levels &&  num_monster == 3) {
			//only enforce the minimum if there are enough cards to do so
			//TODO ideally this should be when num_monster >= 3, but that introduces some difficulties that will require significant refactoring
			//     to make the minimums behave more like requirements
			//     (e.g. if num_monster == 4, you could get 2xLevel1 and 2xLevel2, satisfying the maximum for "Monster" but not the minimum for "Level3Monster")
			minimumCards.put("Level1Monster", 1);
			minimumCards.put("Level2Monster", 1);
			minimumCards.put("Level3Monster", 1);
		}
		minimumCards.put("Thunderstone", num_thunderstone);
		minimumCards.put("Hero", num_hero);
		minimumCards.put("Village", num_village);
		
		return minimumCards;
	}
	
	private Map<String,Integer> getMaximumCards(int num_monster, int num_thunderstone, int num_hero, int num_village, boolean village_limits, boolean monster_levels) {
		Map<String,Integer> maximumCards = new HashMap<String,Integer>();
		maximumCards.put("Monster", num_monster);
		if(monster_levels && num_monster <= 3) {
			//only enforce the maximum if there are few enough cards to do so
			maximumCards.put("Level1Monster", 1);
			maximumCards.put("Level2Monster", 1);
			maximumCards.put("Level3Monster", 1);
		}
		maximumCards.put("Treasure", 1);
		maximumCards.put("Trap", 1);
		maximumCards.put("Thunderstone", num_thunderstone);
		maximumCards.put("Hero", num_hero);
		maximumCards.put("Village", num_village);
		if(village_limits && num_village <= 11) {
			//only enforce the maximum if there are few enough cards to do so
			maximumCards.put("Weapon", 3);
			maximumCards.put("Item", 2);
			maximumCards.put("Spell", 3);
			maximumCards.put("Villager", 3);
		}
		
		return maximumCards;
	}
	
	protected boolean chooseCards(Requirement currentRequirement, CardList currentCards) {
		Iterator<? extends Card> matchingCards = cardDb.getMatchingCards(currentRequirement, currentCards);
		while(matchingCards.hasNext()) {
			Card potentialCard = matchingCards.next();
			
			if(!currentCards.addCard(potentialCard)) {
				continue;
			}
			
			if(!chooseCards(potentialCard.getRequirements(), currentCards)) {
				//remove potentialCard and everything that was added after it
				currentCards.removeCardsAfter(potentialCard);
				continue;
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean chooseCards(List<Requirement> currentRequirements, CardList currentCards) {
		for(Requirement currentRequirement : currentRequirements) {
			if(currentRequirement.match(currentCards)) {
				continue;
			}
			
			if(!chooseCards(currentRequirement, currentCards)) {
				return false;
			}
		}
		return true;
	}
	
	private Requirement getRandomCardRequirement(List<String> remainingCardTypes) {
		String cardType = remainingCardTypes.get(random.nextInt(remainingCardTypes.size()));
		return Requirement.buildRequirement("CardType", "CardType", null, cardType);
	}
}
