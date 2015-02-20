package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.requirement.Requirement;

public class HeroCard extends Card {
	private static List<String> randomizerKeys = new ArrayList<String>(1);
	static {
		randomizerKeys.add("Hero");
	}
	
	private int strength;
	
	public HeroCard(String cardName, String setName, String cardText, List<String> attributes, List<String> classes, List<Requirement> requirements, int strength) {
		super(cardName, setName, cardText, attributes, classes, requirements);
		this.strength = strength;
	}

	@Override
	public List<String> getRandomizerKeys() {
		return randomizerKeys;
	}

	public int getStrength() {
		return strength;
	}

	@Override
	public String getCardType() {
		return "";
	}
}
