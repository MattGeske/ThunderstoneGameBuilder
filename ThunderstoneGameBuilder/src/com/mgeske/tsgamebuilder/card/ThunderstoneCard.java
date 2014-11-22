package com.mgeske.tsgamebuilder.card;

public class ThunderstoneCard extends Card {

	public ThunderstoneCard(String cardName, String setName, String cardText) {
		super(cardName, setName, cardText);
	}

	@Override
	public String getRandomizerKey() {
		return "Thunderstone";
	}

}
