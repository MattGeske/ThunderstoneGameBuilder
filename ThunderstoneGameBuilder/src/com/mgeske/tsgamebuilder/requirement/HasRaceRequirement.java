package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.HeroCard;

public class HasRaceRequirement extends Requirement {
	private String race;

	public HasRaceRequirement(String requirementName, String requiredOn, List<String> values) {
		super(requirementName, requiredOn, values);
		race = values.get(0);
	}

	@Override
	protected boolean cardDetailsMatch(Card c) {
		if(!(c instanceof HeroCard)) {
			return false;
		}
		HeroCard heroCard = (HeroCard)c;
		return heroCard.getRace().equals(race);
	}

	public String getRace() {
		return race;
	}
}
