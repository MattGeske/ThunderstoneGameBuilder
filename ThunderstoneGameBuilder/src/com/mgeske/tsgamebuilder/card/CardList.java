package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.os.Parcel;
import android.os.Parcelable;

public class CardList implements Parcelable {
	private List<DungeonCard> dungeonCards = new ArrayList<DungeonCard>();
	private List<GuardianCard> guardianCards = new ArrayList<GuardianCard>();
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
		if("Monster".equals(cardType) || "Treasure".equals(cardType) || "Trap".equals(cardType)) {
			return getDungeonCards();
		} else if(cardType != null && cardType.startsWith("Level")) {
			return getDungeonCards();
		} else if("Guardian".equals(cardType)) {
			return getGuardianCards();
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
	
	@SuppressWarnings("unchecked")
	private <T extends Card> List<T> getCardListForCard(T card) {
		if(card instanceof DungeonCard) {
			return (List<T>) dungeonCards;
		} else if(card instanceof GuardianCard) {
			return (List<T>) guardianCards;
		} else if(card instanceof ThunderstoneCard) {
			return (List<T>) thunderstoneCards;
		} else if(card instanceof HeroCard) {
			return (List<T>) heroCards;
		} else if(card instanceof VillageCard) {
			return (List<T>) villageCards;
		} else {
			return null;
		}
	}
	
	public <T extends Card> boolean contains(T card) {
		List<T> cards = getCardListForCard(card);
		return cards.contains(card);
	}

	public List<DungeonCard> getDungeonCards() {
		return dungeonCards;
	}
	
	public List<GuardianCard> getGuardianCards() {
		return guardianCards;
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
		return addCard(card, true);
	}
	
	public <T extends Card> boolean addCard(T card, boolean obeyMaximums) {
		if(card == null) {
			return false;
		}

		//make sure it wouldn't violate the maximums
		if(obeyMaximums && !cardCanBeAdded(card)) {
			return false;
		}
		
		//add it to the appropriate list
		List<T> cards = getCardListForCard(card);
		if(cards == null || cards.contains(card)) {
			return false;
		}
		cards.add(card);
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
			removeCard(lastCard);
			if(lastCard.equals(card)) {
				break;
			}
		}
	}
	
	public <T extends Card> void removeCard(T card) {
		List<T> cards = getCardListForCard(card);
		if(cards == null || !cards.contains(card)) {
			return;
		}
		cards.remove(card);
		
		for(String key : card.getRandomizerKeys()) {
			int currentAmount = foundKeys.get(key);
			foundKeys.put(key, currentAmount-1);
		}
	}

	@Override
	public String toString() {
		return "CardList [dungeonCards=" + dungeonCards + ", guardianCards=" + guardianCards
				+ ", thunderstoneCards=" + thunderstoneCards + ", heroCards="
				+ heroCards + ", villageCards=" + villageCards + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(dungeonCards);
		dest.writeTypedList(guardianCards);
		dest.writeTypedList(thunderstoneCards);
		dest.writeTypedList(heroCards);
		dest.writeTypedList(villageCards);
		//TODO write minimumCards/maximumCards/foundKeys here?
		//TODO write cardOrder here? or make removeCardsAfter handle the possibility that a card might
		//     be the list but not in cardOrder?
	}
	
	private CardList(Parcel parcel) {
		parcel.readTypedList(dungeonCards, DungeonCard.CREATOR);
		parcel.readTypedList(guardianCards, GuardianCard.CREATOR);
		parcel.readTypedList(thunderstoneCards, ThunderstoneCard.CREATOR);
		parcel.readTypedList(heroCards, HeroCard.CREATOR);
		parcel.readTypedList(villageCards, VillageCard.CREATOR);
	}
	
	public static final Parcelable.Creator<CardList> CREATOR = new Parcelable.Creator<CardList>() {
		public CardList createFromParcel(Parcel parcel) {
			return new CardList(parcel);
		}
		public CardList[] newArray(int size) {
			return new CardList[size];
		}
	};
}
