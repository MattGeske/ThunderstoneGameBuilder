package com.mgeske.tsgamebuilder.card;

import java.util.List;

public class CardList {
	private List<Card> dungeonCards;
	private List<Card> heroCards;
	private List<Card> villageCards;
	
	public CardList(List<Card> dungeonCards, List<Card> heroCards, List<Card> villageCards) {
		this.dungeonCards = dungeonCards;
		this.heroCards = heroCards;
		this.villageCards = villageCards;
	}

	public List<Card> getDungeonCards() {
		return dungeonCards;
	}

	public List<Card> getHeroCards() {
		return heroCards;
	}

	public List<Card> getVillageCards() {
		return villageCards;
	}
	
	
}
