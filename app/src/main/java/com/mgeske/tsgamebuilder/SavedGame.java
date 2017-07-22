package com.mgeske.tsgamebuilder;

import android.text.TextUtils;

public class SavedGame {
	private String gameName;
	private String gameSource;
	private String[] requiredSetNames;
	
	public SavedGame(String gameName, String gameSource, String[] requiredSetNames) {
		this.gameName = gameName;
		this.gameSource = gameSource;
		this.requiredSetNames = requiredSetNames;
	}

	public String getGameName() {
		return gameName;
	}
	
	public String getGameSource() {
		return gameSource;
	}

	public String getRequiredSetNamesString() {
		return TextUtils.join(", ", requiredSetNames);
	}
	
	public String[] getRequiredSetNamesArray() {
		return requiredSetNames;
	}
}
