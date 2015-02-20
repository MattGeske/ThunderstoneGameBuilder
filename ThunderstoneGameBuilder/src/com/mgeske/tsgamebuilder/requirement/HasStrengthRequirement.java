package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.HeroCard;

public class HasStrengthRequirement extends Requirement {
	private int requiredStrength;
	protected HasStrengthRequirement(String requirementName, String requiredOn, List<String> values) {
		super(requirementName, requiredOn, values);
		String raw_requiredStrength = values.get(0);
		requiredStrength = Integer.parseInt(raw_requiredStrength);
	}
	
	public int getStrength() {
		return requiredStrength;
	}

	@Override
	protected boolean cardDetailsMatch(Card c) {
		if(!(c instanceof HeroCard)) {
			return false;
		}
		HeroCard hero = (HeroCard)c;
		return hero.getStrength() >= requiredStrength;
	}
}
