package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;

public class SpecificCardTypeRequirement extends Requirement {
	/**
	 * CardTypeRequirement intentionally only checks general categories of card type (e.g. a "Monster"
	 *    requirement may actually return a Treasure or Trap, which allows for more randomness
	 *    when choosing cards. SpecificCardTypeRequirement ensures that the caller gets the exact card
	 *    type requested (a "Monster" requirement will return a Monster, a "Treasure" requirement
	 *    will return a Treasure, etc.)
	 */

	public SpecificCardTypeRequirement(String requirementName, String requiredOn, List<String> values) {
		super(requirementName, requiredOn, values);
	}

	@Override
	protected boolean cardDetailsMatch(Card c) {
		return c.getRandomizerKeys().contains(getRequiredOn());
	}

}
