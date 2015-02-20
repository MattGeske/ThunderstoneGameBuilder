package com.mgeske.tsgamebuilder.card;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mgeske.tsgamebuilder.testutil.CardGenerator;

import android.test.AndroidTestCase;

import static com.mgeske.tsgamebuilder.testutil.Assert.*;

public class CardListTest extends AndroidTestCase {
	
	public void testAddDungeonCard() {
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		DungeonCard card = CardGenerator.getDungeonCards(1, 0, 0, null, 1).get(0);
		cardList.addCard(card);
		assertContains(cardList.getDungeonCards(), card);
	}
	
	public void testAddThunderstoneCard() {
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		ThunderstoneCard card = CardGenerator.getThunderstoneCards(1, null).get(0);
		cardList.addCard(card);
		assertContains(cardList.getThunderstoneCards(), card);
	}
	
	public void testAddHeroCard() {
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		HeroCard card = CardGenerator.getHeroCards(1, null).get(0);
		cardList.addCard(card);
		assertContains(cardList.getHeroCards(), card);
	}
	
	public void testAddVillageCard() {
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		VillageCard card = CardGenerator.getVillageCards(1, 0, 0, 0).get(0);
		cardList.addCard(card);
		assertContains(cardList.getVillageCards(), card);
	}
	
	public void testAddOverMaximum() {
		List<HeroCard> heroCards = CardGenerator.getHeroCards(2, null);
		HeroCard hero1 = heroCards.get(0);
		HeroCard hero2 = heroCards.get(1);
		
		Map<String,Integer> maximums = new HashMap<String,Integer>();
		maximums.put("Hero", 1);
		
		CardList cardList = new CardList(new HashMap<String,Integer>(), maximums);
		
		boolean result = cardList.addCard(hero1);
		assertTrue(result);
		assertContains(cardList.getHeroCards(), hero1);
		
		result = cardList.addCard(hero2);
		assertFalse(result);
		assertContains(cardList.getHeroCards(), hero1);
		assertDoesNotContain(cardList.getHeroCards(), hero2);
		
		cardList.removeCardsAfter(hero1);
		result = cardList.addCard(hero2);
		assertTrue(result);
		assertContains(cardList.getHeroCards(), hero2);
		assertDoesNotContain(cardList.getHeroCards(), hero1);
	}
	
	public void testRemoveCardsAfter() {
		CardList cardList = new CardList(new HashMap<String,Integer>(), new HashMap<String,Integer>());
		VillageCard villageCard = CardGenerator.getVillageCards(1, 0, 0, 0).get(0);
		HeroCard heroCard = CardGenerator.getHeroCards(1, null).get(0);
		ThunderstoneCard thunderstoneCard = CardGenerator.getThunderstoneCards(1, null).get(0);
		DungeonCard dungeonCard = CardGenerator.getDungeonCards(1, 0, 0, null, 1).get(0);
		
		cardList.addCard(villageCard);
		cardList.addCard(heroCard);
		cardList.addCard(thunderstoneCard);
		cardList.addCard(dungeonCard);
		assertContains(cardList.getVillageCards(), villageCard);
		assertContains(cardList.getHeroCards(), heroCard);
		assertContains(cardList.getThunderstoneCards(), thunderstoneCard);
		assertContains(cardList.getDungeonCards(), dungeonCard);
		
		cardList.removeCardsAfter(dungeonCard);
		assertContains(cardList.getVillageCards(), villageCard);
		assertContains(cardList.getHeroCards(), heroCard);
		assertContains(cardList.getThunderstoneCards(), thunderstoneCard);
		assertDoesNotContain(cardList.getDungeonCards(), dungeonCard);
		
		cardList.removeCardsAfter(heroCard);
		assertContains(cardList.getVillageCards(), villageCard);
		assertDoesNotContain(cardList.getHeroCards(), heroCard);
		assertDoesNotContain(cardList.getThunderstoneCards(), thunderstoneCard);
		assertDoesNotContain(cardList.getDungeonCards(), dungeonCard);
	}
	
	public void test_remainingMinimums() {
		List<HeroCard> heroCards = CardGenerator.getHeroCards(2, null);
		HeroCard hero1 = heroCards.get(0);
		HeroCard hero2 = heroCards.get(1);
		VillageCard villageCard = CardGenerator.getVillageCards(1, 0, 0, 0).get(0);
		
		Map<String,Integer> minimums = new HashMap<String,Integer>();
		minimums.put("Hero", 2);
		minimums.put("Village", 1);
		CardList cardList = new CardList(minimums, new HashMap<String,Integer>());
		
		assertTrue(cardList.hasRemainingMinimums());
		assertEquals(2, cardList.getRemainingMinimums().size());
		assertContains(cardList.getRemainingMinimums(), "Hero");
		assertContains(cardList.getRemainingMinimums(), "Village");
		
		cardList.addCard(hero1);
		assertTrue(cardList.hasRemainingMinimums());
		assertEquals(2, cardList.getRemainingMinimums().size());
		assertContains(cardList.getRemainingMinimums(), "Hero");
		assertContains(cardList.getRemainingMinimums(), "Village");
		
		cardList.addCard(hero2);
		assertTrue(cardList.hasRemainingMinimums());
		assertEquals(1, cardList.getRemainingMinimums().size());
		assertContains(cardList.getRemainingMinimums(), "Village");
		
		cardList.addCard(villageCard);
		assertFalse(cardList.hasRemainingMinimums());
		assertEquals(0, cardList.getRemainingMinimums().size());
		
		cardList.removeCardsAfter(villageCard);
		assertTrue(cardList.hasRemainingMinimums());
		assertEquals(1, cardList.getRemainingMinimums().size());
		assertContains(cardList.getRemainingMinimums(), "Village");
		
		cardList.removeCardsAfter(hero2);
		assertTrue(cardList.hasRemainingMinimums());
		assertEquals(2, cardList.getRemainingMinimums().size());
		assertContains(cardList.getRemainingMinimums(), "Hero");
		assertContains(cardList.getRemainingMinimums(), "Village");
	}
}
