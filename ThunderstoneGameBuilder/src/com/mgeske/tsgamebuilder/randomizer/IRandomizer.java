package com.mgeske.tsgamebuilder.randomizer;

import android.content.Context;

import com.mgeske.tsgamebuilder.card.CardList;

public interface IRandomizer {
	public CardList generateCardList(Context context);
}
