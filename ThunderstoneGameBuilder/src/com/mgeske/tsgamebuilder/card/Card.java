package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.mgeske.tsgamebuilder.requirement.Requirement;

public abstract class Card implements Comparable<Card>,Parcelable {
	private String cardId;
	private String cardName;
	private String setName;
	private String cardText;
	private List<String> attributes;
	private List<String> classes;
	private List<Requirement> requirements;
	
	protected Card(String cardId, String cardName, String setName, String cardText, List<String> attributes,
			       List<String> classes, List<Requirement> requirements) {
		this.cardId = cardId;
		this.cardName = cardName;
		this.setName = setName;
		this.cardText = cardText;
		this.attributes = new ArrayList<String>(attributes);
		this.classes = classes;
		this.requirements = requirements;
	}
	
	public String getCardId() {
		return cardId;
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

	public List<Requirement> getRequirements() {
		return requirements;
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

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(cardId);
		dest.writeString(cardName);
		dest.writeString(setName);
		dest.writeString(cardText);
		dest.writeStringList(attributes);
		dest.writeStringList(classes);
		//TODO write requirements here?
	}
	
	protected Card(Parcel parcel) {
		cardId = parcel.readString();
		cardName = parcel.readString();
		setName = parcel.readString();
		cardText = parcel.readString();
		attributes = new ArrayList<String>();
		parcel.readStringList(attributes);
		classes = new ArrayList<String>();
		parcel.readStringList(classes);
	}
	
	public abstract List<String> getRandomizerKeys();
	
	public abstract String getCardType();
}
