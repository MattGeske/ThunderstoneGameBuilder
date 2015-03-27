package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.mgeske.tsgamebuilder.requirement.Requirement;

public class GuardianCard extends Card {
	private static List<String> randomizerKeys = new ArrayList<String>(1);
	static {
		randomizerKeys.add("Guardian");
	}

	public GuardianCard(String cardId, String cardName, String setName, String setAbbreviation,
			String cardText, List<String> attributes, List<String> classes,
			List<Requirement> requirements) {
		super(cardId, cardName, setName, setAbbreviation, cardText, attributes, classes,
				requirements);
	}

	@Override
	public List<String> getRandomizerKeys() {
		return randomizerKeys;
	}
	
	@Override
	public String getCardType() {
		return "Guardian";
	}

	@Override
	public String getCardSubtype() {
		return "";
	}

	public GuardianCard(Parcel parcel) {
		super(parcel);
	}

	public static final Parcelable.Creator<GuardianCard> CREATOR = new Parcelable.Creator<GuardianCard>() {
		public GuardianCard createFromParcel(Parcel parcel) {
			return new GuardianCard(parcel);
		}
		public GuardianCard[] newArray(int size) {
			return new GuardianCard[size];
		}
	};

}
