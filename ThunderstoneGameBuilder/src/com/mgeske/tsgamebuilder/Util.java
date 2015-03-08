package com.mgeske.tsgamebuilder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Util {
	public static <V extends Object> boolean arrayContains(V[] arr, V value) {
		for(V arrValue : arr) {
			if(value == arrValue || (value != null && value.equals(arrValue))) {
				return true;
			}
		}
		return false;
	}
	
	public static String[] getChosenSets(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String chosenSetsString = preferences.getString("chosenSets", "");
		return chosenSetsString.split(",");
	}
}
