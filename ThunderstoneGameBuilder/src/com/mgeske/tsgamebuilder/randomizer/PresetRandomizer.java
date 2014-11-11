package com.mgeske.tsgamebuilder.randomizer;

import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.MonsterCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneBearerCard;
import com.mgeske.tsgamebuilder.card.VillageCard;

/**
 * A "randomizer" that returns preset card lists
 *
 */
public class PresetRandomizer implements IRandomizer {
	public PresetRandomizer(String presetName) {
		
	}
	
	public CardList generateCardList() {
		//TODO - for now this just returns the TS Advance First Game Setup
		//dungeon cards
		List<Card> dungeonCards = new ArrayList<Card>();
		dungeonCards.add(new MonsterCard("Burnmarked - Fire", "", ""));
		dungeonCards.add(new MonsterCard("Ogre - Humanoid", "", ""));
		dungeonCards.add(new MonsterCard("Kobold - Humanoid", "", ""));
		dungeonCards.add(new ThunderstoneBearerCard("Stramst", "", ""));
		
		//hero cards
		List<Card> heroCards = new ArrayList<Card>();
		heroCards.add(new HeroCard("Bhoidwood", "", "", 0));
		heroCards.add(new HeroCard("Criochan", "", "", 0));
		heroCards.add(new HeroCard("Drua", "", "", 0));
		heroCards.add(new HeroCard("Thundermage", "", "", 0));
		
		//village cards
		List<Card> villageCards = new ArrayList<Card>();
		villageCards.add(new VillageCard("Battle-scarred Soldier", "", "", 0));
		villageCards.add(new VillageCard("Bounty Hunter", "", "", 0));
		villageCards.add(new VillageCard("Falcon Arbalest", "", "", 0));
		villageCards.add(new VillageCard("King Caelan's Writ", "", "", 0));
		villageCards.add(new VillageCard("Mass Teleport", "", "", 0));
		villageCards.add(new VillageCard("Moonstone", "", "", 0));
		villageCards.add(new VillageCard("Snakehead Flail", "", "", 0));
		villageCards.add(new VillageCard("Summon Storm", "", "", 0));
		
		CardList cardList = new CardList(dungeonCards, heroCards, villageCards);
		return cardList;
	}
}
