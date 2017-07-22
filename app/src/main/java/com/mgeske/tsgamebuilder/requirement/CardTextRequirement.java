package com.mgeske.tsgamebuilder.requirement;

import java.util.List;

import android.annotation.SuppressLint;
import com.mgeske.tsgamebuilder.card.Card;

public class CardTextRequirement extends Requirement {
	private String searchText;
	
	protected CardTextRequirement(String requirementName, String cardType, List<String> values) {
		super(requirementName, cardType, values);
		searchText = values.get(0);
	}
	
	public String getSearchText() {
		return searchText;
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected boolean cardDetailsMatch(Card c) {
		String searchTextLower = searchText.toLowerCase();
		String cardNameLower = c.getCardName().toLowerCase();
		String cardTextLower = c.getCardText().toLowerCase();
		return (cardNameLower.contains(searchTextLower) || cardTextLower.contains(searchTextLower));
	}

}
