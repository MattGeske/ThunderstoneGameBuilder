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

	@Override
	public void setLimits(int num_monster, int num_thunderstone, int num_hero, int num_village, boolean village_limits, boolean monster_levels) {
		// TODO Auto-generated method stub
		
	}
}
