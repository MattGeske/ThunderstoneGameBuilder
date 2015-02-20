package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.requirement.Requirement;

public class VillageCard extends Card {
	private int cost;
	private Integer value = null;
	private Integer weight = null;
	
	public VillageCard(String cardName, String setName, String cardText, List<String> attributes, List<String> classes, List<Requirement> requirements, int cost, Integer weight) {
		super(cardName, setName, cardText, attributes, classes, requirements);
		this.cost = cost;
		this.weight = weight;
	}
	
	public int getCost() {
		return cost;
	}

	public Integer getValue() {
		return value;
	}

	public Integer getWeight() {
		return weight;
	}

	@Override
	public List<String> getRandomizerKeys() {
		List<String> keys = new ArrayList<String>();
		keys.add("Village");
		keys.addAll(getClasses());
		
		String cardName = getCardName();
		if("Elite Militia".equals(cardName) || "Village Thief".equals(cardName)) {
			//handle a couple cards that don't have the "Villager" keyword but, for all intents and purposes, belong with the villagers
			keys.add("Villager");
		}
		return keys;
	}

	@Override
	public String getCardType() {
		List<String> cardTypes = new ArrayList<String>();
		cardTypes.add("Weapon");
		cardTypes.add("Item");
		cardTypes.add("Spell");
		cardTypes.add("Villager");
		for(String cardClass : getRandomizerKeys()) {
			if(cardTypes.contains(cardClass)) {
				return cardClass;
			}
		}
		return "";
	}
}
