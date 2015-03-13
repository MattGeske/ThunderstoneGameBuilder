package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.mgeske.tsgamebuilder.requirement.Requirement;

public class HeroCard extends Card {
	private static List<String> randomizerKeys = new ArrayList<String>(1);
	static {
		randomizerKeys.add("Hero");
	}
	
	private int strength;
	
	public HeroCard(String cardId, String cardName, String setName, String setAbbreviation, String cardText,
			List<String> attributes, List<String> classes, List<Requirement> requirements, int strength) {
		super(cardId, cardName, setName, setAbbreviation, cardText, attributes, classes, requirements);
		this.strength = strength;
	}

	@Override
	public List<String> getRandomizerKeys() {
		return randomizerKeys;
	}

	public int getStrength() {
		return strength;
	}

	@Override
	public String getCardType() {
		return "";
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(strength);
	}
	
	private HeroCard(Parcel parcel) {
		super(parcel);
		strength = parcel.readInt();
	}

	public static final Parcelable.Creator<HeroCard> CREATOR = new Parcelable.Creator<HeroCard>() {
		public HeroCard createFromParcel(Parcel parcel) {
			return new HeroCard(parcel);
		}
		public HeroCard[] newArray(int size) {
			return new HeroCard[size];
		}
	};
}
