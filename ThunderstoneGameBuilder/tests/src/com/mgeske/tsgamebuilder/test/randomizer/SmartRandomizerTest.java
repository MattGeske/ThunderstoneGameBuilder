package com.mgeske.tsgamebuilder.test.randomizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.test.AndroidTestCase;

import com.mgeske.tsgamebuilder.CardDatabase;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.Requirement;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;
import com.mgeske.tsgamebuilder.randomizer.SmartRandomizer;

import static org.mockito.Mockito.*;


public class SmartRandomizerTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	protected static <T> void assertContains(Collection<T> collection, T item) {
		assertTrue("Item '"+item+"' not found in collection "+collection, collection.contains(item));
	}
	
	protected static void assertInRange(int num, int lowerBound, int upperBound) {
		String message = num+" is not in the range ("+lowerBound+", "+upperBound+")";
		assertTrue(message, num >= lowerBound);
		assertTrue(message, num <= upperBound);
	}
	
	protected static CardList getAndAssertCardList(SmartRandomizer randomizer) {
		return getAndAssertCardList(randomizer, 3, 1, 4, 8, true, true);
	}
	
	protected static CardList getAndAssertCardList(SmartRandomizer randomizer, int num_monster, int num_thunderstone, int num_hero, int num_village, boolean village_limits, boolean monster_levels) {
		CardList cardList = randomizer.generateCardList(num_monster, num_thunderstone, num_hero, num_village, village_limits, monster_levels);
		
		Map<String,Integer> cardTypeCounts = new HashMap<String,Integer>();
		cardTypeCounts.put("Monster", 0);
		cardTypeCounts.put("Treasure", 0);
		cardTypeCounts.put("Trap", 0);
		Map<Integer,Integer> monsterLevelCounts = new HashMap<Integer,Integer>();
		monsterLevelCounts.put(1, 0);
		monsterLevelCounts.put(2, 0);
		monsterLevelCounts.put(3, 0);
		for(DungeonCard card : cardList.getDungeonCards()) {
			String cardType = card.getDungeonType();
			
			int currentCount = cardTypeCounts.get(cardType);
			currentCount += 1;
			cardTypeCounts.put(cardType, currentCount);
			
			if("Monster".equals(cardType)) {
				currentCount = monsterLevelCounts.get(card.getLevel());
				currentCount += 1;
				monsterLevelCounts.put(card.getLevel(), currentCount);
			}
		}
		
		//check the number of each type of monster card
		assertEquals(num_monster, cardTypeCounts.get("Monster").intValue());
		assertTrue(cardTypeCounts.get("Treasure") >= 0);
		assertTrue(cardTypeCounts.get("Treasure") <= 1);
		assertTrue(cardTypeCounts.get("Trap") >= 0);
		assertTrue(cardTypeCounts.get("Trap") <= 1);
		if(monster_levels) {
			if(num_monster == 3) {
				assertEquals(1, monsterLevelCounts.get(1).intValue());
				assertEquals(1, monsterLevelCounts.get(2).intValue());
				assertEquals(1, monsterLevelCounts.get(3).intValue());
			} else if(num_monster < 3) {
				assertInRange(monsterLevelCounts.get(1).intValue(), 0, 1);
				assertInRange(monsterLevelCounts.get(2).intValue(), 0, 1);
				assertInRange(monsterLevelCounts.get(3).intValue(), 0, 1);
			} //TODO ideally we'd like to test something if num_monster > 3 as well, but currently the randomizer can't handle that
		}
		
		//check the number of the other types of cards
		assertEquals(num_thunderstone, cardList.getThunderstoneCards().size());
		assertEquals(num_hero, cardList.getHeroCards().size());
		assertEquals(num_village, cardList.getVillageCards().size());
		
		//validate the TS Advance village limits if needed
		if(village_limits && num_village <= 11) {
			Map<String,Integer> villageTypeCounts = new HashMap<String,Integer>();
			villageTypeCounts.put("Weapon", 0);
			villageTypeCounts.put("Item", 0);
			villageTypeCounts.put("Spell", 0);
			villageTypeCounts.put("Villager", 0);
			for(VillageCard card : cardList.getVillageCards()) {
				for(String cardClass : card.getClasses()) {
					if(!villageTypeCounts.containsKey(cardClass)) {
						continue;
					}
					int currentCount = villageTypeCounts.get(cardClass);
					currentCount += 1;
					villageTypeCounts.put(cardClass, currentCount);
				}
			}
			assertInRange(villageTypeCounts.get("Weapon"), 0, 3);
			assertInRange(villageTypeCounts.get("Item"), 0, 2);
			assertInRange(villageTypeCounts.get("Spell"), 0, 3);
			assertInRange(villageTypeCounts.get("Villager"), 0, 3);
		}
		
		return cardList;
	}
	
	/*
	 * Defaults for something resembling a basic thunderstone set: 8 monsters, 1 treasure, 1 trap, 2 thunderstones, 11 heroes, 19 village cards
	 */
	private List<DungeonCard> getDungeonCards() {
		return getDungeonCards(8, 1, 1, null, null);
	}
	private List<DungeonCard> getDungeonCards(Requirement globalRequirement) {
		return getDungeonCards(8, 1, 1, globalRequirement, null);
	}
	
	private List<DungeonCard> getMonsters(int num_monster, int level) {
		return getDungeonCards(num_monster, 0, 0, null, level);
	}
	
	private List<ThunderstoneCard> getThunderstoneCards() {
		return getThunderstoneCards(2, null);
	}
	
	private List<ThunderstoneCard> getThunderstoneCards(Requirement globalRequirement) {
		return getThunderstoneCards(2, globalRequirement);
	}
	
	private List<HeroCard> getHeroCards() {
		return getHeroCards(11, null);
	}
	
	private List<HeroCard> getHeroCards(Requirement globalRequirement) {
		return getHeroCards(11, globalRequirement);
	}
	
	private List<VillageCard> getVillageCards() {
		//a basic thunderstone set has roughly 6 weapons, 4 items, 4 spells, and 5 villagers
		return getVillageCards(6, 4, 4, 5);
	}
	
	private List<DungeonCard> getDungeonCards(int total_monster, int total_treasure, int total_trap, Requirement globalRequirement, Integer level) {
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
			DungeonCard monster = new DungeonCard("Monster "+i, "test", "", "Monster", monster_level, attributes, classes, requirements);
			dungeonCards.add(monster);
		}
		
		//create some treasure cards
		for(int i = 0; i < total_treasure; i++) {
			DungeonCard treasure = new DungeonCard("Treasure "+i, "test", "", "Treasure", null, attributes, classes, requirements);
			dungeonCards.add(treasure);
		}
		
		//create some trap cards
		for(int i = 0; i < total_trap; i++) {
			DungeonCard trap = new DungeonCard("Trap "+i, "test", "", "Trap", null, attributes, classes, requirements);
			dungeonCards.add(trap);
		}
		
		return dungeonCards;
	}
	private List<ThunderstoneCard> getThunderstoneCards(int total_thunderstone, Requirement globalRequirement) {
		List<Requirement> requirements = new ArrayList<Requirement>();
		if(globalRequirement != null) {
			requirements.add(globalRequirement);
		}
		List<ThunderstoneCard> thunderstoneCards = new ArrayList<ThunderstoneCard>();
		for(int i = 0; i < total_thunderstone; i++) {
			ThunderstoneCard thunderstone = new ThunderstoneCard("Thunderstone "+i, "test", "", new ArrayList<String>(), new ArrayList<String>(), requirements);
			thunderstoneCards.add(thunderstone);
		}
		
		return thunderstoneCards;
	}
	
	private List<HeroCard> getHeroCards(int total_hero, Requirement globalRequirement) {
		List<Requirement> requirements = new ArrayList<Requirement>();
		if(globalRequirement != null) {
			requirements.add(globalRequirement);
		}
		List<HeroCard> heroCards = new ArrayList<HeroCard>();
		for(int i = 0; i < total_hero; i++) {
			HeroCard hero = new HeroCard("Hero "+i, "test", "", new ArrayList<String>(), new ArrayList<String>(), requirements, 5);
			heroCards.add(hero);
		}
		
		return heroCards;
	}
	
	private List<VillageCard> getVillageCards(int total_weapons, int total_items, int total_spells, int total_villagers) {
		List<VillageCard> villageCards = new ArrayList<VillageCard>();
		List<String> attributes = new ArrayList<String>();
		List<Requirement> requirements = new ArrayList<Requirement>();

		List<String> classes = new ArrayList<String>();
		classes.add("Weapon");
		for(int i = 0; i < total_weapons; i++) {
			VillageCard village = new VillageCard("Weapon "+i, "test", "", attributes, classes, requirements, 3, null);
			villageCards.add(village);
		}
		
		classes = new ArrayList<String>();
		classes.add("Item");
		for(int i = 0; i < total_items; i++) {
			VillageCard village = new VillageCard("Item "+i, "test", "", attributes, classes, requirements, 3, null);
			villageCards.add(village);
		}
		
		classes = new ArrayList<String>();
		classes.add("Spell");
		for(int i = 0; i < total_spells; i++) {
			VillageCard village = new VillageCard("Spell "+i, "test", "", attributes, classes, requirements, 3, null);
			villageCards.add(village);
		}
		
		classes = new ArrayList<String>();
		classes.add("Villager");
		for(int i = 0; i < total_villagers; i++) {
			VillageCard village = new VillageCard("Villager "+i, "test", "", attributes, classes, requirements, 3, null);
			villageCards.add(village);
		}
		
		return villageCards;
	}
	
	private SmartRandomizer getConfiguredRandomizer(List<DungeonCard> dungeonCards, List<ThunderstoneCard> thunderstoneCards, List<HeroCard> heroCards, List<VillageCard> villageCards) {
		if(dungeonCards == null) {
			dungeonCards = getDungeonCards();
		}
		if(thunderstoneCards == null) {
			thunderstoneCards = getThunderstoneCards();
		}
		if(heroCards == null) {
			heroCards = getHeroCards();
		}
		if(villageCards == null) {
			villageCards = getVillageCards();
		}
		CardDatabase mockDb = mock(CardDatabase.class);
		when(mockDb.getAllDungeonCards()).thenReturn(dungeonCards);
		when(mockDb.getAllThunderstoneCards()).thenReturn(thunderstoneCards);
		when(mockDb.getAllHeroCards()).thenReturn(heroCards);
		when(mockDb.getAllVillageCards()).thenReturn(villageCards);
		SmartRandomizer randomizer = new SmartRandomizer(null);
		randomizer.setCards(dungeonCards, thunderstoneCards, heroCards, villageCards);
		return randomizer;
	}
	
	public void testMonsterRequiresHero() {
		//add a requirement to every monster requiring magic attack, and add the magic attack attribute to exactly one hero - that hero should always be the one chosen
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("HAS_MAGIC_ATTACK");
		Requirement requireMagicAttack = Requirement.buildRequirement("REQUIRES_MAGIC_ATTACK", "HasAnyAttributes", attributeNames, "Hero");
		List<DungeonCard> dungeonCards = getDungeonCards(requireMagicAttack);
		List<HeroCard> heroCards = getHeroCards();
		HeroCard magicAttackHero = new HeroCard("MagicAttackHero", "test", "", attributeNames, new ArrayList<String>(), new ArrayList<Requirement>(), 1);
		heroCards.add(magicAttackHero);
		SmartRandomizer randomizer = getConfiguredRandomizer(dungeonCards, null, heroCards, null);
		CardList cardList = getAndAssertCardList(randomizer, 3, 1, 1, 8, true, true);
		
		List<HeroCard> chosenHeroCards = cardList.getHeroCards();
		assertContains(chosenHeroCards, magicAttackHero);
	}
	
	public void testMonsterRequiresVillage() {
		//add a requirement to every monster requiring light, and add the light attribute to exactly one village card - that card should always be the one chosen
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("HAS_LIGHT");
		Requirement requireLight = Requirement.buildRequirement("REQUIRES_LIGHT", "HasAnyAttributes", attributeNames, "Village");
		List<DungeonCard> dungeonCards = getDungeonCards(requireLight);
		List<VillageCard> villageCards = getVillageCards();
		VillageCard lightCard = new VillageCard("LightCard", "test", "", attributeNames, new ArrayList<String>(), new ArrayList<Requirement>(), 1, null);
		villageCards.add(lightCard);
		SmartRandomizer randomizer = getConfiguredRandomizer(dungeonCards, null, null, villageCards);
		CardList cardList = getAndAssertCardList(randomizer, 3, 1, 4, 1, true, true);
		
		List<VillageCard> chosenVillageCards = cardList.getVillageCards();
		assertContains(chosenVillageCards, lightCard);
	}
	
	public void testThunderstoneRequiresHero() {
		//add a requirement to every thunderstone bearer requiring magic attack, and add the magic attack attribute to exactly one hero - that hero should always be the one chosen
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("HAS_MAGIC_ATTACK");
		Requirement requireMagicAttack = Requirement.buildRequirement("REQUIRES_MAGIC_ATTACK", "HasAnyAttributes", attributeNames, "Hero");
		List<ThunderstoneCard> thunderstoneCards = getThunderstoneCards(requireMagicAttack);
		List<HeroCard> heroCards = getHeroCards();
		HeroCard magicAttackHero = new HeroCard("MagicAttackHero", "test", "", attributeNames, new ArrayList<String>(), new ArrayList<Requirement>(), 1);
		heroCards.add(magicAttackHero);
		SmartRandomizer randomizer = getConfiguredRandomizer(null, thunderstoneCards, heroCards, null);
		CardList cardList = getAndAssertCardList(randomizer, 3, 1, 1, 8, true, true);
		
		List<HeroCard> chosenHeroCards = cardList.getHeroCards();
		assertContains(chosenHeroCards, magicAttackHero);
	}
	
	public void testThunderstoneRequiresVillage() {
		//add a requirement to every thunderstone bearer requiring light, and add the light attribute to exactly one village card - that card should always be the one chosen
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("HAS_LIGHT");
		Requirement requireLight = Requirement.buildRequirement("REQUIRES_LIGHT", "HasAnyAttributes", attributeNames, "Village");
		List<ThunderstoneCard> thunderstoneCards = getThunderstoneCards(requireLight);
		List<VillageCard> villageCards = getVillageCards();
		VillageCard lightCard = new VillageCard("LightCard", "test", "", attributeNames, new ArrayList<String>(), new ArrayList<Requirement>(), 1, null);
		villageCards.add(lightCard);
		SmartRandomizer randomizer = getConfiguredRandomizer(null, thunderstoneCards, null, villageCards);
		CardList cardList = getAndAssertCardList(randomizer, 3, 1, 4, 1, true, true);
		
		List<VillageCard> chosenVillageCards = cardList.getVillageCards();
		assertContains(chosenVillageCards, lightCard);
	}
	
	public void testHeroRequiresVillage() {
		//add a requirement to every hero requiring a bow, and add the bow class to exactly one village card - that card should always be the one chosen
		List<String> classNames = new ArrayList<String>();
		classNames.add("Bow");
		Requirement requireLight = Requirement.buildRequirement("WANTS_BOW", "HasAllClasses", classNames, "Village");
		List<HeroCard> heroCards = getHeroCards(requireLight);
		List<VillageCard> villageCards = getVillageCards();
		VillageCard bowCard = new VillageCard("BowCard", "test", "", new ArrayList<String>(), classNames, new ArrayList<Requirement>(), 1, null);
		villageCards.add(bowCard);
		SmartRandomizer randomizer = getConfiguredRandomizer(null, null, heroCards, villageCards);
		CardList cardList = getAndAssertCardList(randomizer, 3, 1, 4, 1, true, true);
		
		List<VillageCard> chosenVillageCards = cardList.getVillageCards();
		assertContains(chosenVillageCards, bowCard);
	}
	
	private void _testVillageLimitBase(String villageType, boolean village_limits) {
		/**
		 * Creates a set of cards with a large number of the specified type and very few of the other types. When village_limits is true, there should be
		 * the maximum of the specified village type (2 for Items, 3 for everything else). When village_limits is false, all the chosen cards should be of that type. 
		 */
		int num_weapon;
		int num_item;
		int num_spell;
		int num_villager;
		if(village_limits) {
			//we need just enough cards to force the randomizer to hit the limit on the specified type, without having so few cards that it can't generate a game without violating the maximum
			num_weapon = 2;
			num_item = 1;
			num_spell = 2;
			num_villager = 2;
		} else {
			//there is no maximum, so we'll make all the cards be the specified type and the randomizer should still be able to generate a valid game
			num_weapon = 0;
			num_item = 0;
			num_spell = 0;
			num_villager = 0;
		}
		
		if("Weapon".equals(villageType)) {
			num_weapon += 20;
		} else if("Item".equals(villageType)) {
			num_item += 20;
		} else if("Spell".equals(villageType)) {
			num_spell += 20;
		} else if("Villager".equals(villageType)) {
			num_villager += 20;
		}
		List<VillageCard> villageCards = getVillageCards(num_weapon, num_item, num_spell, num_villager);
		
		SmartRandomizer randomizer = getConfiguredRandomizer(null, null, null, villageCards);
		CardList cardList = getAndAssertCardList(randomizer, 3, 1, 4, 8, village_limits, true);
		
		List<VillageCard> chosenVillageCards = cardList.getVillageCards();
		int count = 0;
		for(VillageCard card : chosenVillageCards) {
			if(card.getClasses().contains(villageType)) {
				count += 1;
			}
		}
		
		if(village_limits) {
			if("Item".equals(villageType)) {
				assertEquals(2, count);
			} else {
				assertEquals(3, count);
			}
		} else {
			assertEquals(8, count);
		}
	}
	
	public void testVillageLimitOnWeapon() {
		_testVillageLimitBase("Weapon", true);
	}
	
	public void testVillageLimitOnItem() {
		_testVillageLimitBase("Item", true);
	}
	
	public void testVillageLimitOnSpell() {
		_testVillageLimitBase("Spell", true);
	}
	
	public void testVillageLimitOnVillager() {
		_testVillageLimitBase("Villager", true);
	}
	
	public void testVillageLimitOffWeapon() {
		_testVillageLimitBase("Weapon", false);
	}
	
	public void testVillageLimitOffItem() {
		_testVillageLimitBase("Item", false);
	}
	
	public void testVillageLimitOffSpell() {
		_testVillageLimitBase("Spell", false);
	}
	
	public void testVillageLimitOffVillager() {
		_testVillageLimitBase("Villager", false);
	}
	
	public void testMonsterLevelsOn() {
		//creates a set of dungeon cards with lots of level 1 monsters and exactly one each of level 2 and 3; the level 2 and 3 monsters should always be in the results
		List<DungeonCard> level1 = getMonsters(10, 1);
		List<DungeonCard> level2 = getMonsters(1, 2);
		List<DungeonCard> level3 = getMonsters(1, 3);
		
		List<DungeonCard> allMonsters = new ArrayList<DungeonCard>();
		allMonsters.addAll(level1);
		allMonsters.addAll(level2);
		allMonsters.addAll(level3);
		
		SmartRandomizer randomizer = getConfiguredRandomizer(allMonsters, null, null, null);
		CardList cardList = getAndAssertCardList(randomizer, 3, 1, 4, 8, true, true);
		
		assertContains(cardList.getDungeonCards(), level2.get(0));
		assertContains(cardList.getDungeonCards(), level3.get(0));
	}
	
	public void testMonsterLevelsOff() {
		//creates a set of dungeon cards with only level 1 monsters - the randomizer should still be able to produce a valid game
		List<DungeonCard> monsters = getMonsters(10, 1);
		
		SmartRandomizer randomizer = getConfiguredRandomizer(monsters, null, null, null);
		getAndAssertCardList(randomizer, 3, 1, 4, 8, true, false); //this does all the asserts we need
	}
}
