package com.mgeske.tsgamebuilder.card;

import java.util.List;

public abstract class Requirement {
	public static Requirement buildRequirement(String requirementName, String requirementType, List<String> values, String requiredOn) {
		if("HasAnyAttributes".equals(requirementType)) {
			return new HasAnyAttributesRequirement(requirementName, requiredOn, values);
		} else if("HasAnyClasses".equals(requirementType)) {
			return new HasAnyClassesRequirement(requirementName, requiredOn, values);
		} else if("HasAllClasses".equals(requirementType)) {
			return new HasAllClassesRequirement(requirementName, requiredOn, values);
		} else if("Placeholder".equals(requirementType)) {
			return new PlaceholderRequirement(requirementName, requiredOn, values);
		} else {
			throw new RuntimeException("Unknown requirement type: "+requirementType);
		}
	}
	
	
	private String requirementName;
	private String cardType;
	private List<String> values;

	protected Requirement(String requirementName, String cardType, List<String> values) {
		this.requirementName = requirementName;
		this.cardType = cardType;
		this.values = values;
	}
	
	public String getRequirementName() {
		return requirementName;
	}

	public String getCardType() {
		return cardType;
	}

	public List<String> getValues() {
		return values;
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

class PlaceholderRequirement extends Requirement {
	protected PlaceholderRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
	}
	
	@Override
	public boolean match(Card c) {
		return true;
	}
}
