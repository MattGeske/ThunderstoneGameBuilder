package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

public class HeroCard extends Card {
	private static List<String> randomizerKeys = new ArrayList<String>(1);
	static {
		randomizerKeys.add("Hero");
	}
	
	public HeroCard(String cardName, String setName, String cardText, List<String> attributes, List<String> classes) {
		super(cardName, setName, cardText, attributes, classes);
	}

	@Override
	public List<String> getRandomizerKeys() {
		return randomizerKeys;
	}

}
