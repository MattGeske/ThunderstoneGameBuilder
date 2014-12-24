package com.mgeske.tsgamebuilder.randomizer;

import android.content.Context;

import com.mgeske.tsgamebuilder.card.CardList;

public interface IRandomizer {
	public CardList generateCardList(Context context);
	public void setLimits(int num_monster, int num_thunderstone, int num_hero, int num_village);
}
