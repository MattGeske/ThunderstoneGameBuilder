package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.VillageCard;

public class HasWeightRequirement extends Requirement {
	private int requiredWeight;
	public HasWeightRequirement(String requirementName, String requiredOn, List<String> values) {
		super(requirementName, requiredOn, values);
		String raw_requiredWeight = values.get(0);
		requiredWeight = Integer.parseInt(raw_requiredWeight);
	}
	
	public int getWeight() {
		return requiredWeight;
	}

	@Override
	protected boolean cardDetailsMatch(Card c) {
		if(!(c instanceof VillageCard)) {
			return false;
		}
		VillageCard villageCard = (VillageCard)c;
		return villageCard.getWeight() != null && villageCard.getWeight() == requiredWeight;
	}

}
