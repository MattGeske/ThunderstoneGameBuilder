package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.VillageCard;

public class CardValueRequirement extends Requirement {
	private int value;
	public CardValueRequirement(String requirementName, String requiredOn, List<String> values) {
		super(requirementName, requiredOn, values);
		String raw_value = values.get(0);
		value = Integer.parseInt(raw_value);
	}
	
	public int getValue() {
		return value;
	}

	@Override
	protected boolean cardDetailsMatch(Card c) {
		if(!(c instanceof VillageCard)) {
			return false;
		}
		VillageCard villageCard = (VillageCard)c;
		return villageCard.getValue() != null && villageCard.getValue() == value;
	}

}
