package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;

public class CardTypeRequirement extends Requirement {
	protected CardTypeRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}
	
	@Override
	protected boolean cardDetailsMatch(Card c) {
		//superclass already checks card type, so nothing to do here
		return true;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+" [requirementName=" + getRequirementName() + ", requiredOn="+ getRequiredOn() + "]";
	}
}
