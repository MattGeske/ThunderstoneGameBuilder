package com.mgeske.tsgamebuilder.card;

public class VillageCard extends Card {
	private int cost;
	private Integer value = null;
	private Integer light = null;
	
	public VillageCard(String cardName, String setName, String cardText, int cost) {
		super(cardName, setName, cardText);
		this.cost = cost;
	}
	
	public int getCost() {
		return cost;
	}

	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	public Integer getLight() {
		return light;
	}
	public void setLight(Integer light) {
		this.light = light;
	}

	@Override
	public String getRandomizerKey() {
		String cardName = getCardName();
		if("Elite Militia".equals(cardName) || "Village Thief".equals(cardName)) {
			//handle a couple cards that don't have the "Villager" key but, for all intents and purposes, belong with the villagers
//			return "Villager";
		}
		return "Village";
	}
}
