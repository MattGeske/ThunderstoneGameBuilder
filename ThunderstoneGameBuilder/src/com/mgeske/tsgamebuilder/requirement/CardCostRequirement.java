package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.VillageCard;

public class CardCostRequirement extends Requirement {
	private int cost;
	public CardCostRequirement(String requirementName, String requiredOn, List<String> values) {
		super(requirementName, requiredOn, values);
		String raw_cost = values.get(0);
		cost = Integer.parseInt(raw_cost);
	}
	
	public int getCost() {
		return cost;
	}

	@Override
	protected boolean cardDetailsMatch(Card c) {
		if(!(c instanceof VillageCard)) {
			return false;
		}
		VillageCard villageCard = (VillageCard)c;
		return villageCard.getCost() == cost;
	}

}
