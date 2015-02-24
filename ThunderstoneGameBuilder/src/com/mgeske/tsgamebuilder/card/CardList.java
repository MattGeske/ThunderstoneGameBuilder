package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class CardList {
	private List<DungeonCard> dungeonCards = new ArrayList<DungeonCard>();
	private List<ThunderstoneCard> thunderstoneCards = new ArrayList<ThunderstoneCard>();
	private List<HeroCard> heroCards = new ArrayList<HeroCard>();
	private List<VillageCard> villageCards = new ArrayList<VillageCard>();
	private Stack<Card> cardOrder = new Stack<Card>();
	
	private Map<String,Integer> minimumCards;
	private Map<String,Integer> maximumCards;
	private Map<String,Integer> foundKeys;
	
	public CardList(Map<String,Integer> minimumCards, Map<String,Integer> maximumCards) {
		this.minimumCards = minimumCards;
		this.maximumCards = maximumCards;
		foundKeys = new HashMap<String,Integer>();
	}
	
	public List<? extends Card> getCardsByType(String cardType) {
		if("Monster".equals(cardType)) {
			return getDungeonCards();
		} else if(cardType != null && cardType.startsWith("Level")) {
			return getDungeonCards();
		} else if("Thunderstone".equals(cardType)) {
			return getThunderstoneCards();
		} else if("Hero".equals(cardType)) {
			return getHeroCards();
		} else if("Village".equals(cardType)) {
			return getVillageCards();
		} else {
			throw new RuntimeException("Unexpected card type: "+cardType);
		}
	}

	public List<DungeonCard> getDungeonCards() {
		return dungeonCards;
	}

	public List<HeroCard> getHeroCards() {
		return heroCards;
	}

	public List<VillageCard> getVillageCards() {
		return villageCards;
	}

	public List<ThunderstoneCard> getThunderstoneCards() {
		return thunderstoneCards;
	}
	
	public boolean hasRemainingMinimums() {
		for(String key : minimumCards.keySet()) {
			if(!foundKeys.containsKey(key)) {
				return true;
			}
			int absoluteMinimum = minimumCards.get(key);
			int currentAmount = foundKeys.get(key);
			if(absoluteMinimum - currentAmount > 0) {
				return true;
			}
		}
		return false;
	}
	
	public List<String> getRemainingMinimums() {
		List<String> minimums = new ArrayList<String>();
		for(String key : minimumCards.keySet()) {
			if(!foundKeys.containsKey(key)) {
				minimums.add(key);
				continue;
			}
			int absoluteMinimum = minimumCards.get(key);
			int currentAmount = foundKeys.get(key);
			if(absoluteMinimum - currentAmount > 0) {
				minimums.add(key);
			}
		}
		return minimums;
	}
	
	private Integer getCurrentMaximum(String key) {
		if(!maximumCards.containsKey(key)) {
			return null;
		}
		int absoluteMax = maximumCards.get(key);
		if(!foundKeys.containsKey(key)) {
			return absoluteMax;
		}
		int currentAmount = foundKeys.get(key);
		return absoluteMax - currentAmount;
	}
	
	public boolean addCard(Card card) {
		//make sure it wouldn't violate the maximums
		if(!cardCanBeAdded(card)) {
			return false;
		}
		
		//add it to the appropriate list
		if(card instanceof DungeonCard) {
			dungeonCards.add((DungeonCard)card);
		} else if(card instanceof ThunderstoneCard) {
			thunderstoneCards.add((ThunderstoneCard)card);
		} else if(card instanceof HeroCard) {
			heroCards.add((HeroCard)card);
		} else if(card instanceof VillageCard) {
			villageCards.add((VillageCard)card);
		} else {
			return false;
		}
		cardOrder.push(card);
		
		//update foundKeys
		for(String key : card.getRandomizerKeys()) {
			if(!foundKeys.containsKey(key)) {
				foundKeys.put(key, 1);
			} else {
				int currentAmount = foundKeys.get(key);
				foundKeys.put(key, currentAmount+1);
			}
		}
		
		return true;
	}
	
	private boolean cardCanBeAdded(Card card) {
		for(String key : card.getRandomizerKeys()) {
			Integer currentMax = getCurrentMaximum(key);
			if(currentMax != null && currentMax <= 0) {
				return false;
			}
		}
		return true;
	}
	
	public void removeCardsAfter(Card card) {
		//removes the given card, as well as any cards that were added after it
		if(!cardOrder.contains(card)) {
			return;
		}
		while(!cardOrder.isEmpty()) {
			Card lastCard = cardOrder.pop();
			
			if(lastCard instanceof DungeonCard) {
				dungeonCards.remove(lastCard);
			} else if(lastCard instanceof ThunderstoneCard) {
				thunderstoneCards.remove(lastCard);
			} else if(lastCard instanceof HeroCard) {
				heroCards.remove(lastCard);
			} else if(lastCard instanceof VillageCard) {
				villageCards.remove(lastCard);
			}
			
			for(String key : lastCard.getRandomizerKeys()) {
				int currentAmount = foundKeys.get(key);
				foundKeys.put(key, currentAmount-1);
			}
			
			if(lastCard.equals(card)) {
				break;
			}
		}
	}
}
