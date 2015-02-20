package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;

public class HasAnyAttributesRequirement extends Requirement {
	protected HasAnyAttributesRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}

	@Override
	protected boolean cardDetailsMatch(Card c) {
		for(String attributeName : getValues()) {
			if(c.getAttributes().contains(attributeName)) {
				return true;
			}
		}
		return false;
	}
}
