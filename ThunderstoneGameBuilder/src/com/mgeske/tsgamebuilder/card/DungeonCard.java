package com.mgeske.tsgamebuilder.card;

import java.util.ArrayList;
import java.util.List;

public class DungeonCard extends Card {
	private String dungeonType;
	private Integer level;
	
	public DungeonCard(String cardName, String setName, String cardText, String dungeonType, Integer level, List<String> attributes, List<String> classes, List<Requirement> requirements) {
		super(cardName, setName, cardText, attributes, classes, requirements);
		this.dungeonType = dungeonType;
		this.level = level;
	}

	public Integer getLevel() {
		return level;
	}

	public String getDungeonType() {
		return dungeonType;
	}
	
	@Override
	public String getCardType() {
		if(level != null) {
			return "Level "+level;
		}
		return dungeonType;
	}

	@Override
	public List<String> getRandomizerKeys() {
		List<String> keys = new ArrayList<String>(2); //we know there will never be more than 2 things in the list
		String dungeonType = getDungeonType();
		if("Monster".equals(dungeonType)) {
			keys.add("Level"+getLevel()+dungeonType);
		}
		keys.add(dungeonType);
		return keys;
	}

	
}
