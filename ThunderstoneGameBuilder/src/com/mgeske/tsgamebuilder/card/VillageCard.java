package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.mgeske.tsgamebuilder.requirement.Requirement;

public class VillageCard extends Card {
	private int cost;
	private Integer value = null;
	private Integer weight = null;
	
	public VillageCard(String cardId, String cardName, String setName, String setAbbreviation, String cardText,
			List<String> attributes, List<String> classes, List<Requirement> requirements, int cost, Integer value, Integer weight) {
		super(cardId, cardName, setName, setAbbreviation, cardText, attributes, classes, requirements);
		this.cost = cost;
		this.value = value;
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
	/**
	 * 
	private int cost;
	private Integer value = null;
	private Integer weight = null;
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(cost);
		dest.writeValue(value);
		dest.writeValue(weight);
	}
	
	private VillageCard(Parcel parcel) {
		super(parcel);
		cost = parcel.readInt();
		value = (Integer)parcel.readValue(null);
		weight = (Integer)parcel.readValue(null);
	}

	public static final Parcelable.Creator<VillageCard> CREATOR = new Parcelable.Creator<VillageCard>() {
		public VillageCard createFromParcel(Parcel parcel) {
			return new VillageCard(parcel);
		}
		public VillageCard[] newArray(int size) {
			return new VillageCard[size];
		}
	};
}
