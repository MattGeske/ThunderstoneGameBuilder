package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.DungeonCard;

public class MonsterLevelRequirement extends Requirement {
	private int monsterLevel;

	public MonsterLevelRequirement(String requirementName, String requiredOn, List<String> values) {
		super(requirementName, requiredOn, values);
		String raw_monsterLevel = values.get(0);
		monsterLevel = Integer.parseInt(raw_monsterLevel);
	}

	@Override
	protected boolean cardDetailsMatch(Card c) {
		if(!(c instanceof DungeonCard)) {
			return false;
		}
		DungeonCard dungeonCard = (DungeonCard)c;
		return "Monster".equals(dungeonCard.getCardType()) && monsterLevel == dungeonCard.getLevel();
	}
	
	public int getMonsterLevel() {
		return monsterLevel;
	}

}
