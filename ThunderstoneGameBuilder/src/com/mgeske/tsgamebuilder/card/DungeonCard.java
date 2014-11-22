package com.mgeske.tsgamebuilder.card;

public class DungeonCard extends Card {
	private String cardType;
	private Integer level;
	
	public DungeonCard(String cardName, String setName, String cardText, String cardType, Integer level) {
		super(cardName, setName, cardText);
		this.cardType = cardType;
		this.level = level;
	}

	public Integer getLevel() {
		return level;
	}

	public String getCardType() {
		return cardType;
	}

	@Override
	public String getRandomizerKey() {
		String cardType = getCardType();
		if("Monster".equals(cardType)) {
			return "Level"+getLevel()+cardType;
		}
		return cardType;
	}

	
}
