package com.mgeske.tsgamebuilder.requirement;

import java.util.List;
import java.util.logging.Logger;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.GuardianCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;

public abstract class Requirement {
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	public static Requirement buildRequirement(String requirementName, String requirementType, List<String> values, String requiredOn) {
		if("HasAnyAttributes".equals(requirementType)) {
			return new HasAnyAttributesRequirement(requirementName, requiredOn, values);
		} else if("HasAnyClasses".equals(requirementType)) {
			return new HasAnyClassesRequirement(requirementName, requiredOn, values);
		} else if("HasAllClasses".equals(requirementType)) {
			return new HasAllClassesRequirement(requirementName, requiredOn, values);
		} else if("HasStrength".equals(requirementType)) {
			return new HasStrengthRequirement(requirementName, requiredOn, values);
		} else if("HasRace".equals(requirementType)) {
			return new HasRaceRequirement(requirementName, requiredOn, values);
		} else if("LightweightEdgedWeapon".equals(requirementType)) {
			return new LightweightEdgedWeaponRequirement(requirementName, requiredOn, values);
		} else if("CardType".equals(requirementType)) {
			return new CardTypeRequirement(requirementName, requiredOn, values);
		} else if("SpecificCardType".equals(requirementType)) {
			return new SpecificCardTypeRequirement(requirementName, requiredOn, values);
		} else if("CardText".equals(requirementType)) {
			return new CardTextRequirement(requirementName, requiredOn, values);
		} else if("MonsterLevel".equals(requirementType)) {
			return new MonsterLevelRequirement(requirementName, requiredOn, values);
		} else {
			throw new RuntimeException("Unknown requirement type: "+requirementType);
		}
	}
	
	
	private String requirementName;
	private String requiredOn;
	private List<String> values;

	protected Requirement(String requirementName, String requiredOn, List<String> values) {
		this.requirementName = requirementName;
		this.requiredOn = requiredOn;
		this.values = values;
	}
	
	public String getRequirementName() {
		return requirementName;
	}

	public String getRequiredOn() {
		return requiredOn;
	}

	public List<String> getValues() {
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((requiredOn == null) ? 0 : requiredOn.hashCode());
		result = prime * result
				+ ((requirementName == null) ? 0 : requirementName.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Requirement other = (Requirement) obj;
		if (requiredOn == null) {
			if (other.requiredOn != null)
				return false;
		} else if (!requiredOn.equals(other.requiredOn))
			return false;
		if (requirementName == null) {
			if (other.requirementName != null)
				return false;
		} else if (!requirementName.equals(other.requirementName))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()+" [requirementName=" + requirementName + ", values="+ values + "]";
	}
	
	public boolean match(CardList cardList) {
		List<? extends Card> cards = cardList.getCardsByType(requiredOn);
		
		for(Card card : cards) {
			if(match(card)) {
				return true;
			}
		}
		return false;
	}

	public boolean match(Card c) {
		if(("Monster".equals(requiredOn) && c instanceof DungeonCard) ||
				(requiredOn.startsWith("Level") && c instanceof DungeonCard) ||
				("Treasure".equals(requiredOn) && c instanceof DungeonCard) ||
				("Trap".equals(requiredOn) && c instanceof DungeonCard) ||
				("Guardian".equals(requiredOn) && c instanceof GuardianCard) ||
				("Thunderstone".equals(requiredOn) && c instanceof ThunderstoneCard) ||
				("Hero".equals(requiredOn) && c instanceof HeroCard) ||
				("Village".equals(requiredOn) && c instanceof VillageCard)) {
			return cardDetailsMatch(c);
		}
		return false;
	}
	
	protected abstract boolean cardDetailsMatch(Card c);
}
