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
}
