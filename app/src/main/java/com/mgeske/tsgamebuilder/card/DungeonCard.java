package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.mgeske.tsgamebuilder.requirement.Requirement;

public class DungeonCard extends Card {
	private String dungeonType;
	private Integer level;
	
	public DungeonCard(String cardId, String cardName, String setName, String setAbbreviation, String cardText,
			String dungeonType, Integer level, List<String> attributes, List<String> classes,
			List<Requirement> requirements) {
		super(cardId ,cardName, setName, setAbbreviation, cardText, attributes, classes, requirements);
		this.dungeonType = dungeonType;
		this.level = level;
	}

	public Integer getLevel() {
		return level;
	}

	@Override
	public String getCardType() {
		return dungeonType;
	}
	
	@Override
	public String getCardSubtype() {
		if(level != null) {
			return "Level "+level;
		}
		return dungeonType;
	}

	@Override
	public List<String> getRandomizerKeys() {
		List<String> keys = new ArrayList<String>(2); //we know there will never be more than 2 things in the list
		String dungeonType = getCardType();
		if("Monster".equals(dungeonType)) {
			keys.add("Level"+getLevel()+dungeonType);
		}
		keys.add(dungeonType);
		return keys;
	}
	/**
	 * 
	private String dungeonType;
	private Integer level;
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(dungeonType);
		dest.writeValue(level);
	}
	
	private DungeonCard(Parcel parcel) {
		super(parcel);
		dungeonType = parcel.readString();
		level = (Integer)parcel.readValue(null);
	}

	public static final Parcelable.Creator<DungeonCard> CREATOR = new Parcelable.Creator<DungeonCard>() {
		public DungeonCard createFromParcel(Parcel parcel) {
			return new DungeonCard(parcel);
		}
		public DungeonCard[] newArray(int size) {
			return new DungeonCard[size];
		}
	};
}
