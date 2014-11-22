package com.mgeske.tsgamebuilder.card;

public class HeroCard extends Card {
	
	public HeroCard(String cardName, String setName, String cardText) {
		super(cardName, setName, cardText);
	}

	@Override
	public String getRandomizerKey() {
		return "Hero";
	}

}
