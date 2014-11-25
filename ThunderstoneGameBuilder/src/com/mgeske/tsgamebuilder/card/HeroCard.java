package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

public class HeroCard extends Card {
	private static List<String> randomizerKeys = new ArrayList<String>(1);
	private List<String> classes;
	static {
		randomizerKeys.add("Hero");
	}
	
	public HeroCard(String cardName, String setName, String cardText, List<String> attributes, List<String> classes) {
		super(cardName, setName, cardText, attributes);
		this.classes = classes;
		for(String cardClass : classes) {
			addAttribute("IS_"+cardClass.toUpperCase());
		}
	}

	@Override
	public List<String> getRandomizerKeys() {
		return randomizerKeys;
	}

}
