package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.VillageCard;

public class HasAllClassesRequirement extends Requirement {
	protected HasAllClassesRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}

	@Override
	protected boolean cardDetailsMatch(Card c) {
		List<String> classes;
		if(c instanceof VillageCard) {
			classes = ((VillageCard)c).getClasses();
		} else if(c instanceof HeroCard) {
			classes = ((HeroCard)c).getClasses();
		} else {
			return false;
		}
		return classes.containsAll(getValues());
	}
	
}
