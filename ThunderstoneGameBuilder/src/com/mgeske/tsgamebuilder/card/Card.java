package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

public abstract class Card implements Comparable<Card> {
	private String cardName;
	private String setName;
	private String cardText;
	private List<String> attributes;
	private List<String> classes;
	
	protected Card(String cardName, String setName, String cardText, List<String> attributes, List<String> classes) {
		this.cardName = cardName;
		this.setName = setName;
		this.cardText = cardText;
		this.attributes = new ArrayList<String>(attributes);
		this.classes = classes;
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

	public List<String> getAttributes() {
		return attributes;
	}
	
	public List<String> getClasses() {
		return classes;
	}

	protected void addAttribute(String attribute) {
		attributes.add(attribute);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+" '"+cardName+"'";
	}

	@Override
	public int compareTo(Card another) {
		return this.cardName.compareTo(another.cardName);
	}
	
	public abstract List<String> getRandomizerKeys();
	
	
}
