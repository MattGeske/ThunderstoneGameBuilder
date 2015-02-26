package com.mgeske.tsgamebuilder.testutil;

import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;
import com.mgeske.tsgamebuilder.requirement.Requirement;

public class CardGenerator {

	/*
	 * Defaults for something resembling a basic thunderstone set: 8 monsters, 1 treasure, 1 trap, 2 thunderstones, 11 heroes, 19 village cards
	 */
	public static List<DungeonCard> getDungeonCards() {
		return getDungeonCards(8, 1, 1, null, null);
	}
	public static List<DungeonCard> getDungeonCards(Requirement globalRequirement) {
		return getDungeonCards(8, 1, 1, globalRequirement, null);
	}
	
	public static List<DungeonCard> getMonsters(int num_monster, int level) {
		return getDungeonCards(num_monster, 0, 0, null, level);
	}
	
	public static List<ThunderstoneCard> getThunderstoneCards() {
		return getThunderstoneCards(2, null);
	}
	
	public static List<ThunderstoneCard> getThunderstoneCards(Requirement globalRequirement) {
		return getThunderstoneCards(2, globalRequirement);
	}
	
	public static List<HeroCard> getHeroCards() {
		return getHeroCards(11, null);
	}
	
	public static List<HeroCard> getHeroCards(Requirement globalRequirement) {
		return getHeroCards(11, globalRequirement);
	}
	
	public static List<VillageCard> getVillageCards() {
		//a basic thunderstone set has roughly 6 weapons, 4 items, 4 spells, and 5 villagers
		return getVillageCards(6, 4, 4, 5);
	}
	
	public static List<DungeonCard> getDungeonCards(int total_monster, int total_treasure, int total_trap, Requirement globalRequirement, Integer level) {
		List<String> attributes = new ArrayList<String>();
		List<String> classes = new ArrayList<String>();
		List<Requirement> requirements = new ArrayList<Requirement>();
		if(globalRequirement != null) {
			requirements.add(globalRequirement);
		}
		
		List<DungeonCard> dungeonCards = new ArrayList<DungeonCard>();
		for(int i = 0; i < total_monster; i++) {
			int monster_level;
			if(level != null) {
				monster_level = level;
			} else {
				monster_level = (i%3)+1;
			}
			String cardId = Integer.toString(i);
			DungeonCard monster = new DungeonCard(cardId, "Monster "+i, "test", "", "Monster", monster_level, attributes, classes, requirements);
			dungeonCards.add(monster);
		}
		
		//create some treasure cards
		for(int i = 0; i < total_treasure; i++) {
			String cardId = Integer.toString(i);
			DungeonCard treasure = new DungeonCard(cardId, "Treasure "+i, "test", "", "Treasure", null, attributes, classes, requirements);
			dungeonCards.add(treasure);
		}
		
		//create some trap cards
		for(int i = 0; i < total_trap; i++) {
			String cardId = Integer.toString(i);
			DungeonCard trap = new DungeonCard(cardId, "Trap "+i, "test", "", "Trap", null, attributes, classes, requirements);
			dungeonCards.add(trap);
		}
		
		return dungeonCards;
	}
	public static List<ThunderstoneCard> getThunderstoneCards(int total_thunderstone, Requirement globalRequirement) {
		List<Requirement> requirements = new ArrayList<Requirement>();
		if(globalRequirement != null) {
			requirements.add(globalRequirement);
		}
		List<ThunderstoneCard> thunderstoneCards = new ArrayList<ThunderstoneCard>();
		for(int i = 0; i < total_thunderstone; i++) {
			String cardId = Integer.toString(i);
			ThunderstoneCard thunderstone = new ThunderstoneCard(cardId, "Thunderstone "+i, "test", "", new ArrayList<String>(), new ArrayList<String>(), requirements);
			thunderstoneCards.add(thunderstone);
		}
		
		return thunderstoneCards;
	}
	
	public static List<HeroCard> getHeroCards(int total_hero, Requirement globalRequirement) {
		List<Requirement> requirements = new ArrayList<Requirement>();
		if(globalRequirement != null) {
			requirements.add(globalRequirement);
		}
		List<HeroCard> heroCards = new ArrayList<HeroCard>();
		for(int i = 0; i < total_hero; i++) {
			String cardId = Integer.toString(i);
			HeroCard hero = new HeroCard(cardId, "Hero "+i, "test", "", new ArrayList<String>(), new ArrayList<String>(), requirements, 5);
			heroCards.add(hero);
		}
		
		return heroCards;
	}
	
	public static List<VillageCard> getVillageCards(int total_weapons, int total_items, int total_spells, int total_villagers) {
		List<VillageCard> villageCards = new ArrayList<VillageCard>();
		List<String> attributes = new ArrayList<String>();
		List<Requirement> requirements = new ArrayList<Requirement>();

		List<String> classes = new ArrayList<String>();
		classes.add("Weapon");
		for(int i = 0; i < total_weapons; i++) {
			String cardId = Integer.toString(i);
			VillageCard village = new VillageCard(cardId, "Weapon "+i, "test", "", attributes, classes, requirements, 3, null);
			villageCards.add(village);
		}
		
		classes = new ArrayList<String>();
		classes.add("Item");
		for(int i = 0; i < total_items; i++) {
			String cardId = Integer.toString(i);
			VillageCard village = new VillageCard(cardId, "Item "+i, "test", "", attributes, classes, requirements, 3, null);
			villageCards.add(village);
		}
		
		classes = new ArrayList<String>();
		classes.add("Spell");
		for(int i = 0; i < total_spells; i++) {
			String cardId = Integer.toString(i);
			VillageCard village = new VillageCard(cardId, "Spell "+i, "test", "", attributes, classes, requirements, 3, null);
			villageCards.add(village);
		}
		
		classes = new ArrayList<String>();
		classes.add("Villager");
		for(int i = 0; i < total_villagers; i++) {
			String cardId = Integer.toString(i);
			VillageCard village = new VillageCard(cardId, "Villager "+i, "test", "", attributes, classes, requirements, 3, null);
			villageCards.add(village);
		}
		
		return villageCards;
	}
}
