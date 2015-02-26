package com.mgeske.tsgamebuilder.randomizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.test.AndroidTestCase;
import android.util.SparseIntArray;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;
import com.mgeske.tsgamebuilder.db.CardDatabase;
import com.mgeske.tsgamebuilder.randomizer.SmartRandomizer;
import com.mgeske.tsgamebuilder.requirement.Requirement;
import com.mgeske.tsgamebuilder.testutil.CardGenerator;

import static org.mockito.Mockito.*;
import static com.mgeske.tsgamebuilder.testutil.Assert.*;


public class SmartRandomizerTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		//workaround for dexmaker issue on 4.3
		System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
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
		SparseIntArray monsterLevelCounts = new SparseIntArray();
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
				assertEquals(1, monsterLevelCounts.get(1));
				assertEquals(1, monsterLevelCounts.get(2));
				assertEquals(1, monsterLevelCounts.get(3));
			} else if(num_monster < 3) {
				assertInRange(monsterLevelCounts.get(1), 0, 1);
				assertInRange(monsterLevelCounts.get(2), 0, 1);
				assertInRange(monsterLevelCounts.get(3), 0, 1);
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
	
	
	private SmartRandomizer getConfiguredRandomizer(List<DungeonCard> dungeonCards, List<ThunderstoneCard> thunderstoneCards, List<HeroCard> heroCards, List<VillageCard> villageCards) {
		if(dungeonCards == null) {
			dungeonCards = CardGenerator.getDungeonCards();
		}
		if(thunderstoneCards == null) {
			thunderstoneCards = CardGenerator.getThunderstoneCards();
		}
		if(heroCards == null) {
			heroCards = CardGenerator.getHeroCards();
		}
		if(villageCards == null) {
			villageCards = CardGenerator.getVillageCards();
		}
		CardDatabase mockDb = mock(CardDatabase.class);
		when(mockDb.getMatchingCards(any(Requirement.class), any(CardList.class))).then(
				returnMatchingCards(dungeonCards, thunderstoneCards, heroCards, villageCards));
		SmartRandomizer randomizer = new SmartRandomizer(mockDb);
		return randomizer;
	}
	
	private Answer<Iterator<Card>> returnMatchingCards(List<DungeonCard> dungeonCards, List<ThunderstoneCard> thunderstoneCards,
			List<HeroCard> heroCards, List<VillageCard> villageCards) {
		final Map<String,List<? extends Card>> cardTypeMap = new HashMap<String,List<? extends Card>>();
		cardTypeMap.put("Monster", dungeonCards);
		cardTypeMap.put("Level1Monster", dungeonCards);
		cardTypeMap.put("Level2Monster", dungeonCards);
		cardTypeMap.put("Level3Monster", dungeonCards);
		cardTypeMap.put("Thunderstone", thunderstoneCards);
		cardTypeMap.put("Hero", heroCards);
		cardTypeMap.put("Village", villageCards);
		return new Answer<Iterator<Card>>() {
			@Override
			public Iterator<Card> answer(InvocationOnMock invocation) throws Throwable {
				List<Card> matchingCards = new ArrayList<Card>();
				Requirement requirement = invocation.getArgumentAt(0, Requirement.class);
				CardList currentCards = invocation.getArgumentAt(1, CardList.class);
				String cardType = requirement.getRequiredOn();
				List<? extends Card> currentCardsOfSameType = currentCards.getCardsByType(cardType);
				List<? extends Card> allCards = cardTypeMap.get(cardType);
				for(Card card : allCards) {
					if(requirement.match(card) && !currentCardsOfSameType.contains(card)) {
						matchingCards.add(card);
					}
				}
				return matchingCards.iterator();
			}
		};
	}
	
	public void testBasicRandomize() {
		SmartRandomizer randomizer = getConfiguredRandomizer(null, null, null, null);
		getAndAssertCardList(randomizer);
	}
	
	public void testMonsterRequiresHero() {
		//TODO make a base test and use it to modify the other requirement tests
		//force it to choose a monster requiring magic attack, and add the magic attack attribute to exactly one hero - that hero should always be chosen
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("HAS_MAGIC_ATTACK");
		Requirement requireMagicAttack = Requirement.buildRequirement("REQUIRES_MAGIC_ATTACK", "HasAnyAttributes", attributeNames, "Hero");
		List<DungeonCard> dungeonCards = CardGenerator.getDungeonCards(requireMagicAttack);
		
		List<HeroCard> heroCards = CardGenerator.getHeroCards();
		HeroCard magicAttackHero = new HeroCard("1", "MagicAttackHero", "test", "", attributeNames, new ArrayList<String>(), new ArrayList<Requirement>(), 1);
		heroCards.add(magicAttackHero);
		
		SmartRandomizer randomizer = getConfiguredRandomizer(dungeonCards, null, heroCards, null);
		Requirement requireMonster = Requirement.buildRequirement("CardType", "CardType", null, "Monster");
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		boolean result = randomizer.chooseCards(requireMonster, cardList);
		
		assertTrue(result);
		assertEquals(1, cardList.getDungeonCards().size());
		assertEquals(1, cardList.getHeroCards().size());
		assertContains(cardList.getHeroCards(), magicAttackHero);
	}
	
	public void testMonsterRequiresVillage() {
		//add a requirement to every monster requiring light, and add the light attribute to exactly one village card - that card should always be the one chosen
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("HAS_LIGHT");
		Requirement requireLight = Requirement.buildRequirement("REQUIRES_LIGHT", "HasAnyAttributes", attributeNames, "Village");
		List<DungeonCard> dungeonCards = CardGenerator.getDungeonCards(requireLight);
		
		List<VillageCard> villageCards = CardGenerator.getVillageCards();
		VillageCard lightCard = new VillageCard("1", "LightCard", "test", "", attributeNames, new ArrayList<String>(), new ArrayList<Requirement>(), 1, null);
		villageCards.add(lightCard);
		
		SmartRandomizer randomizer = getConfiguredRandomizer(dungeonCards, null, null, villageCards);
		Requirement requireMonster = Requirement.buildRequirement("CardType", "CardType", null, "Monster");
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		boolean result = randomizer.chooseCards(requireMonster, cardList);
		
		assertTrue(result);
		assertEquals(1, cardList.getDungeonCards().size());
		assertEquals(1, cardList.getVillageCards().size());
		assertContains(cardList.getVillageCards(), lightCard);
	}
	
	public void testThunderstoneRequiresHero() {
		//add a requirement to every thunderstone bearer requiring magic attack, and add the magic attack attribute to exactly one hero - that hero should always be the one chosen
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("HAS_MAGIC_ATTACK");
		Requirement requireMagicAttack = Requirement.buildRequirement("REQUIRES_MAGIC_ATTACK", "HasAnyAttributes", attributeNames, "Hero");
		List<ThunderstoneCard> thunderstoneCards = CardGenerator.getThunderstoneCards(requireMagicAttack);
		
		List<HeroCard> heroCards = CardGenerator.getHeroCards();
		HeroCard magicAttackHero = new HeroCard("1", "MagicAttackHero", "test", "", attributeNames, new ArrayList<String>(), new ArrayList<Requirement>(), 1);
		heroCards.add(magicAttackHero);
		
		SmartRandomizer randomizer = getConfiguredRandomizer(null, thunderstoneCards, heroCards, null);
		Requirement requireMonster = Requirement.buildRequirement("CardType", "CardType", null, "Thunderstone");
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		boolean result = randomizer.chooseCards(requireMonster, cardList);
		
		assertTrue(result);
		assertEquals(1, cardList.getThunderstoneCards().size());
		assertEquals(1, cardList.getHeroCards().size());
		assertContains(cardList.getHeroCards(), magicAttackHero);
	}
	
	public void testThunderstoneRequiresVillage() {
		//add a requirement to every thunderstone bearer requiring light, and add the light attribute to exactly one village card - that card should always be the one chosen
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("HAS_LIGHT");
		Requirement requireLight = Requirement.buildRequirement("REQUIRES_LIGHT", "HasAnyAttributes", attributeNames, "Village");
		List<ThunderstoneCard> thunderstoneCards = CardGenerator.getThunderstoneCards(requireLight);
		
		List<VillageCard> villageCards = CardGenerator.getVillageCards();
		VillageCard lightCard = new VillageCard("1", "LightCard", "test", "", attributeNames, new ArrayList<String>(), new ArrayList<Requirement>(), 1, null);
		villageCards.add(lightCard);
		
		SmartRandomizer randomizer = getConfiguredRandomizer(null, thunderstoneCards, null, villageCards);
		Requirement requireMonster = Requirement.buildRequirement("CardType", "CardType", null, "Thunderstone");
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		boolean result = randomizer.chooseCards(requireMonster, cardList);
		
		assertTrue(result);
		assertEquals(1, cardList.getThunderstoneCards().size());
		assertEquals(1, cardList.getVillageCards().size());
		assertContains(cardList.getVillageCards(), lightCard);
	}
	
	public void testHeroRequiresVillage() {
		//add a requirement to every hero requiring a bow, and add the bow class to exactly one village card - that card should always be the one chosen
		List<String> classNames = new ArrayList<String>();
		classNames.add("Bow");
		Requirement requireLight = Requirement.buildRequirement("WANTS_BOW", "HasAllClasses", classNames, "Village");
		List<HeroCard> heroCards = CardGenerator.getHeroCards(requireLight);
		
		List<VillageCard> villageCards = CardGenerator.getVillageCards();
		VillageCard bowCard = new VillageCard("1", "BowCard", "test", "", new ArrayList<String>(), classNames, new ArrayList<Requirement>(), 1, null);
		villageCards.add(bowCard);
		
		SmartRandomizer randomizer = getConfiguredRandomizer(null, null, heroCards, villageCards);
		Requirement requireMonster = Requirement.buildRequirement("CardType", "CardType", null, "Hero");
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		boolean result = randomizer.chooseCards(requireMonster, cardList);
		
		assertTrue(result);
		assertEquals(1, cardList.getHeroCards().size());
		assertEquals(1, cardList.getVillageCards().size());
		assertContains(cardList.getVillageCards(), bowCard);
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
		List<VillageCard> villageCards = CardGenerator.getVillageCards(num_weapon, num_item, num_spell, num_villager);
		
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
		List<DungeonCard> level1 = CardGenerator.getMonsters(10, 1);
		List<DungeonCard> level2 = CardGenerator.getMonsters(1, 2);
		List<DungeonCard> level3 = CardGenerator.getMonsters(1, 3);
		
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
		List<DungeonCard> monsters = CardGenerator.getMonsters(10, 1);
		
		SmartRandomizer randomizer = getConfiguredRandomizer(monsters, null, null, null);
		getAndAssertCardList(randomizer, 3, 1, 4, 8, true, false); //this does all the asserts we need
	}
}
