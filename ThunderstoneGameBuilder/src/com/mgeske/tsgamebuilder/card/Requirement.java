package com.mgeske.tsgamebuilder.card;

import java.util.List;
import java.util.logging.Logger;

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
		} else if("LightweightEdgedWeapon".equals(requirementType)) {
			return new LightweightEdgedWeaponRequirement(requirementName, requiredOn, values);
		} else if("Placeholder".equals(requirementType)) {
			return new PlaceholderRequirement(requirementName, requiredOn, values);
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

	public abstract boolean match(Card c);
}

class HasAnyAttributesRequirement extends Requirement {
	protected HasAnyAttributesRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}

	@Override
	public boolean match(Card c) {
		for(String attributeName : getValues()) {
			if(c.getAttributes().contains(attributeName)) {
				return true;
			}
		}
		return false;
	}
}

class HasAnyClassesRequirement extends Requirement {
	protected HasAnyClassesRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}

	@Override
	public boolean match(Card c) {
		List<String> classes;
		if(c instanceof VillageCard) {
			classes = ((VillageCard)c).getClasses();
		} else if(c instanceof HeroCard) {
			classes = ((HeroCard)c).getClasses();
		} else {
			return false;
		}
		for(String className : getValues()) {
			if(classes.contains(className)) {
				return true;
			}
		}
		return false;
	}
}

class HasAllClassesRequirement extends Requirement {
	protected HasAllClassesRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}

	@Override
	public boolean match(Card c) {
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

class HasStrengthRequirement extends Requirement {
	private int requiredStrength;
	protected HasStrengthRequirement(String requirementName, String requiredOn, List<String> values) {
		super(requirementName, requiredOn, values);
		String raw_requiredStrength = values.get(0);
		requiredStrength = Integer.parseInt(raw_requiredStrength);
	}

	@Override
	public boolean match(Card c) {
		if(!(c instanceof HeroCard)) {
			return false;
		}
		HeroCard hero = (HeroCard)c;
		return hero.getStrength() >= requiredStrength;
	}
}

class LightweightEdgedWeaponRequirement extends Requirement {
	//matches an edged weapon with weight <= 3
	protected LightweightEdgedWeaponRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}
	
	@Override
	public boolean match(Card c) {
		if(!(c instanceof VillageCard)) {
			return false;
		}
		VillageCard villageCard = (VillageCard)c;
		logger.info("Considering card: "+c.getCardName()+"; classes="+c.getClasses()+"; weight="+villageCard.getWeight());
		return villageCard.getClasses().contains("Weapon") && villageCard.getClasses().contains("Edged") && villageCard.getWeight() <= 3;
	}
}

class PlaceholderRequirement extends Requirement {
	protected PlaceholderRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}
	
	@Override
	public boolean match(Card c) {
		return true;
	}
}
