package com.mgeske.tsgamebuilder.card;

public abstract class Card {
	private String cardName;
	private String setName;
	private String cardText;
	
	protected Card(String cardName, String setName, String cardText) {
		this.cardName = cardName;
		this.setName = setName;
		this.cardText = cardText;
	}

	public String getCardName() {
		return cardName;
	}

	public String getSetName() {
		return setName;
	}

	public String getCardText() {
		return cardText;
	}
}
