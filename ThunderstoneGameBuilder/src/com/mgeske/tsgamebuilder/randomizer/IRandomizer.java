package com.mgeske.tsgamebuilder.randomizer;

import com.mgeske.tsgamebuilder.card.CardList;

public interface IRandomizer {
	public CardList generateCardList(int num_monster, int num_thunderstone, int num_hero, int num_village, boolean village_limits, boolean monster_levels);
}
