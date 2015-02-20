package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.requirement.Requirement;

public class ThunderstoneCard extends Card {
	private static List<String> randomizerKeys = new ArrayList<String>(1);
	static {
		randomizerKeys.add("Thunderstone");
	}

	public ThunderstoneCard(String cardName, String setName, String cardText, List<String> attributes, List<String> classes, List<Requirement> requirements) {
		super(cardName, setName, cardText, attributes, classes, requirements);
	}

	@Override
	public List<String> getRandomizerKeys() {
		return randomizerKeys;
	}

	@Override
	public String getCardType() {
		return "";
	}

}
