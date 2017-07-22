package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.VillageCard;

public class LightweightEdgedWeaponRequirement extends Requirement {
	//matches an edged weapon with weight <= 3
	protected LightweightEdgedWeaponRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}
	
	@Override
	protected boolean cardDetailsMatch(Card c) {
		if(!(c instanceof VillageCard)) {
			return false;
		}
		VillageCard villageCard = (VillageCard)c;
		logger.info("Considering card: "+c.getCardName()+"; classes="+c.getClasses()+"; weight="+villageCard.getWeight());
		return villageCard.getClasses().contains("Weapon") && villageCard.getClasses().contains("Edged") && villageCard.getWeight() <= 3;
	}
}
