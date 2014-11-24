package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

public class DungeonCard extends Card {
	private String cardType;
	private Integer level;
	
	public DungeonCard(String cardName, String setName, String cardText, String cardType, Integer level, List<String> attributes) {
		super(cardName, setName, cardText, attributes);
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
	public List<String> getRandomizerKeys() {
		List<String> keys = new ArrayList<String>(1); //we know there will always be exactly one thing in the list
		String cardType = getCardType();
		if("Monster".equals(cardType)) {
			keys.add("Level"+getLevel()+cardType);
		} else {
			keys.add(cardType);
		}
		return keys;
	}

	
}
