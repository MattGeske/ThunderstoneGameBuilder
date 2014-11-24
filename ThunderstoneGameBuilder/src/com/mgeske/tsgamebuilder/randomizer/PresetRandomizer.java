package com.mgeske.tsgamebuilder.randomizer;

import com.mgeske.tsgamebuilder.card.CardList;

import android.content.Context;

/**
 * A "randomizer" that returns preset card lists
 *
 */
public class PresetRandomizer implements IRandomizer {
	public PresetRandomizer(String presetName) {
		
	}
	
	public CardList generateCardList(Context context) {
		//TODO - for now this just returns the TS Advance First Game Setup
		//dungeon cards
		return null;
	}
}
