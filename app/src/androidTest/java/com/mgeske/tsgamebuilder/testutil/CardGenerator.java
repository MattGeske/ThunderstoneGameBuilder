package com.mgeske.tsgamebuilder.testutil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.GuardianCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;
import com.mgeske.tsgamebuilder.requirement.Requirement;

public class CardGenerator {

	/*
	 * Defaults for something resembling a basic thunderstone set: 8 monsters, 1 treasure, 1 trap, 1 guardian, 2 thunderstones, 11 heroes, 19 village cards
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
	
	public static List<GuardianCard> getGuardianCards() {
		return getGuardianCards(1);
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
			String cardId = UUID.randomUUID().toString();
			DungeonCard monster = new DungeonCard(cardId, "Monster "+cardId, "test", "", "", "Monster", monster_level, attributes, classes, requirements);
			dungeonCards.add(monster);
		}
		
		//create some treasure cards
		for(int i = 0; i < total_treasure; i++) {
			String cardId = UUID.randomUUID().toString();
			DungeonCard treasure = new DungeonCard(cardId, "Treasure "+cardId, "test", "", "", "Treasure", null, attributes, classes, requirements);
			dungeonCards.add(treasure);
		}
		
		//create some trap cards
		for(int i = 0; i < total_trap; i++) {
			String cardId = UUID.randomUUID().toString();
			DungeonCard trap = new DungeonCard(cardId, "Trap "+cardId, "test", "", "", "Trap", null, attributes, classes, requirements);
			dungeonCards.add(trap);
		}
		
		return dungeonCards;
	}
	
	public static List<GuardianCard> getGuardianCards(int total_guardian) {
		List<GuardianCard> guardianCards = new ArrayList<GuardianCard>();
		for(int i = 0; i < total_guardian; i++) {
			String cardId = UUID.randomUUID().toString();
			GuardianCard guardian = new GuardianCard(cardId, "Guardian "+cardId, "test", "", "", new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Requirement>());
			guardianCards.add(guardian);
		}
		return guardianCards;
	}
	
	public static List<ThunderstoneCard> getThunderstoneCards(int total_thunderstone, Requirement globalRequirement) {
		List<Requirement> requirements = new ArrayList<Requirement>();
		if(globalRequirement != null) {
			requirements.add(globalRequirement);
		}
		List<ThunderstoneCard> thunderstoneCards = new ArrayList<ThunderstoneCard>();
		for(int i = 0; i < total_thunderstone; i++) {
			String cardId = UUID.randomUUID().toString();
			ThunderstoneCard thunderstone = new ThunderstoneCard(cardId, "Thunderstone "+cardId, "test", "", "", "", new ArrayList<String>(), new ArrayList<String>(), requirements);
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
			String cardId = UUID.randomUUID().toString();
			HeroCard hero = new HeroCard(cardId, "Hero "+cardId, "test", "", "", new ArrayList<String>(), new ArrayList<String>(), requirements, "", 5);
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
			String cardId = UUID.randomUUID().toString();
			VillageCard village = new VillageCard(cardId, "Weapon "+cardId, "test", "", "", attributes, classes, requirements, 3, null, null);
			villageCards.add(village);
		}
		
		classes = new ArrayList<String>();
		classes.add("Item");
		for(int i = 0; i < total_items; i++) {
			String cardId = UUID.randomUUID().toString();
			VillageCard village = new VillageCard(cardId, "Item "+cardId, "test", "", "", attributes, classes, requirements, 3, null, null);
			villageCards.add(village);
		}
		
		classes = new ArrayList<String>();
		classes.add("Spell");
		for(int i = 0; i < total_spells; i++) {
			String cardId = UUID.randomUUID().toString();
			VillageCard village = new VillageCard(cardId, "Spell "+cardId, "test", "", "", attributes, classes, requirements, 3, null, null);
			villageCards.add(village);
		}
		
		classes = new ArrayList<String>();
		classes.add("Villager");
		for(int i = 0; i < total_villagers; i++) {
			String cardId = UUID.randomUUID().toString();
			VillageCard village = new VillageCard(cardId, "Villager "+cardId, "test", "", "", attributes, classes, requirements, 3, null, null);
			villageCards.add(village);
		}
		
		return villageCards;
	}
}
