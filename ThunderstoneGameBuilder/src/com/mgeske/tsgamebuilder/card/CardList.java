package com.mgeske.tsgamebuilder.card;

import java.util.List;

public class CardList {
	private List<DungeonCard> dungeonCards;
	private List<ThunderstoneCard> thunderstoneCards;
	private List<HeroCard> heroCards;
	private List<VillageCard> villageCards;
	
	public CardList(List<DungeonCard> dungeonCards, List<ThunderstoneCard> thunderstoneCards, List<HeroCard> heroCards, List<VillageCard> villageCards) {
		this.dungeonCards = dungeonCards;
		this.thunderstoneCards = thunderstoneCards;
		this.heroCards = heroCards;
		this.villageCards = villageCards;
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
}
