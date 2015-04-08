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
	private String setAbbreviation;
	private String cardText;
	private List<String> attributes;
	private List<String> classes;
	private List<Requirement> requirements;
	
	protected Card(String cardId, String cardName, String setName, String setAbbreviation, String cardText, 
			List<String> attributes, List<String> classes, List<Requirement> requirements) {
		this.cardId = cardId;
		this.cardName = cardName;
		this.setName = setName;
		this.setAbbreviation = setAbbreviation;
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
	
	public String getSetAbbreviation() {
		return setAbbreviation;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cardName == null) ? 0 : cardName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		//assumption: no 2 different cards will have the same name (enforced by unique constraint in db)
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		if (cardName == null) {
			if (other.cardName != null)
				return false;
		} else if (!cardName.equals(other.cardName))
			return false;
		return true;
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
		dest.writeString(setAbbreviation);
		dest.writeString(setName);
		dest.writeString(cardText);
		dest.writeStringList(attributes);
		dest.writeStringList(classes);
		//TODO write requirements here?
	}
	
	protected Card(Parcel parcel) {
		cardId = parcel.readString();
		cardName = parcel.readString();
		setAbbreviation = parcel.readString();
		setName = parcel.readString();
		cardText = parcel.readString();
		attributes = new ArrayList<String>();
		parcel.readStringList(attributes);
		classes = new ArrayList<String>();
		parcel.readStringList(classes);
	}
	
	public abstract List<String> getRandomizerKeys();
	public abstract String getCardType();
	public abstract String getCardSubtype();
}
