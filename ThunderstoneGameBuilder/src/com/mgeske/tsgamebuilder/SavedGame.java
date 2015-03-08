package com.mgeske.tsgamebuilder;

import android.text.TextUtils;

public class SavedGame {
	private String gameName;
	private String[] requiredSetNames;
	
	public SavedGame(String gameName, String[] requiredSetNames) {
		this.gameName = gameName;
		this.requiredSetNames = requiredSetNames;
	}

	public String getGameName() {
		return gameName;
	}

	public String getRequiredSetNamesString() {
		return TextUtils.join(", ", requiredSetNames);
	}
	
	public String[] getRequiredSetNamesArray() {
		return requiredSetNames;
	}
}
