package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.mgeske.tsgamebuilder.requirement.Requirement;

public class ThunderstoneCard extends Card {
	private static List<String> randomizerKeys = new ArrayList<String>(1);
	static {
		randomizerKeys.add("Thunderstone");
	}

	public ThunderstoneCard(String cardId, String cardName, String setName, String setAbbreviation, String cardText,
			List<String> attributes, List<String> classes, List<Requirement> requirements) {
		super(cardId, cardName, setName, setAbbreviation, cardText, attributes, classes, requirements);
	}

	@Override
	public List<String> getRandomizerKeys() {
		return randomizerKeys;
	}

	@Override
	public String getCardType() {
		return "";
	}
	
	private ThunderstoneCard(Parcel parcel) {
		super(parcel);
	}

	public static final Parcelable.Creator<ThunderstoneCard> CREATOR = new Parcelable.Creator<ThunderstoneCard>() {
		public ThunderstoneCard createFromParcel(Parcel parcel) {
			return new ThunderstoneCard(parcel);
		}
		public ThunderstoneCard[] newArray(int size) {
			return new ThunderstoneCard[size];
		}
	};

}
