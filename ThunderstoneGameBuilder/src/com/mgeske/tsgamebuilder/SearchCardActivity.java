package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.db.CardDatabase;
import com.mgeske.tsgamebuilder.requirement.Requirement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SearchCardActivity extends Activity {
	private static final int REQUEST_CODE_SEARCH_RESULT = 1;
	private Map<String,View> searchFieldsMap = new HashMap<String,View>();
	private View currentShownView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_card);
		
		searchFieldsMap.put("Monster", findViewById(R.id.search_monster_fields));
		searchFieldsMap.put("Hero", findViewById(R.id.search_hero_fields));
		searchFieldsMap.put("Village", findViewById(R.id.search_village_fields));
		
		Spinner cardTypeSpinner = (Spinner)findViewById(R.id.search_card_type);
		cardTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String cardType = (String)parent.getItemAtPosition(position);
				updateSearchFields(cardType);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				//do nothing
			}
		});
		
		Button searchButton = (Button)findViewById(R.id.search_card);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchForCard();
			}
		});
	}
	
	private void updateSearchFields(String cardType) {
		if(currentShownView != null) {
			currentShownView.setVisibility(View.GONE);
		}
		currentShownView = searchFieldsMap.get(cardType);
		if(currentShownView != null) {
			currentShownView.setVisibility(View.VISIBLE);
		}
	}
	
	private void searchForCard() {
		//get user input
		Spinner cardTypeSpinner = (Spinner)findViewById(R.id.search_card_type);
		String cardType = (String)cardTypeSpinner.getSelectedItem();
		EditText cardTextSearchField = (EditText)findViewById(R.id.card_text);
		String cardSearchText = cardTextSearchField.getText().toString().trim();
		CheckBox includeAllSetsField = (CheckBox)findViewById(R.id.include_all_sets);
		boolean includeAllSets = includeAllSetsField.isChecked();
		
		//build requirements - append to list in order of most specific to least specific
		List<Requirement> searchRequirements = new ArrayList<Requirement>();
		//search card type specific fields
		addCardTypeSpecificRequirements(cardType, searchRequirements);
		//search card text if specified
		if(cardSearchText != null && !"".equals(cardSearchText)) {
			List<String> requirementValues = new ArrayList<String>();
			requirementValues.add(cardSearchText);
			searchRequirements.add(Requirement.buildRequirement("CardText", "CardText", requirementValues, cardType));
		}
		//always search by specific card type
		searchRequirements.add(Requirement.buildRequirement("SpecificCardType", "SpecificCardType", null, cardType));
		
		
		//do the db query using the first (most specific) requirement
		Requirement searchRequirement = searchRequirements.remove(0);
		CardDatabase cardDb = null;
		List<Card> matchingCards;
		try {
			cardDb = new CardDatabase(this);
			matchingCards = new ArrayList<Card>();
			Iterator<? extends Card> cardIter = cardDb.getMatchingCards(searchRequirement, null, includeAllSets);
			while(cardIter.hasNext()) {
				Card card = cardIter.next();
				matchingCards.add(card);
			}
			Collections.sort(matchingCards);
		} finally {
			if(cardDb != null) {
				cardDb.close();
			}
		}
		
		//filter the cards from the query by applying the rest of the requirements
		ArrayList<Card> cardResults = new ArrayList<Card>();
		for(Card card : matchingCards) {
			boolean matchesAll = true;
			for(Requirement requirement : searchRequirements) {
				if(!requirement.match(card)) {
					matchesAll = false;
					break;
				}
			}
			if(matchesAll) {
				cardResults.add(card);
			}
		}
		
		
		Intent searchResultsIntent = new Intent(this, SearchResultsActivity.class);
		searchResultsIntent.putParcelableArrayListExtra("cardResults", cardResults);
		startActivityForResult(searchResultsIntent, REQUEST_CODE_SEARCH_RESULT);
	}
	
	private void addCardTypeSpecificRequirements(String cardType, List<Requirement> searchRequirements) {
		if("Monster".equals(cardType)) {
			addMonsterRequirements(searchRequirements);
		} else if("Hero".equals(cardType)) {
			addHeroRequirements(searchRequirements);
		} else if("Village".equals(cardType)) {
			addVillageRequirements(searchRequirements);
		}
	}
	
	private void addMonsterRequirements(List<Requirement> searchRequirements) {
		//order of most specific to least specific: doomladen, ambusher, gives disease, monster level
		addAttributeRequirementIfChecked(R.id.search_monster_doomladen, "HAS_DOOMLADEN", "Monster", searchRequirements);
		addAttributeRequirementIfChecked(R.id.search_monster_ambusher, "HAS_AMBUSHER", "Monster", searchRequirements);
		addAttributeRequirementIfChecked(R.id.search_monster_disease, "GIVES_DISEASE", "Monster", searchRequirements);
		
		Spinner monsterLevelSpinner = (Spinner)findViewById(R.id.search_monster_level);
		String monsterLevel = (String)monsterLevelSpinner.getSelectedItem();
		if(monsterLevel != null && !"Any".equals(monsterLevel)) {
			List<String> values = new ArrayList<String>();
			values.add(monsterLevel);
			Requirement levelRequirement = Requirement.buildRequirement("MonsterLevel", "MonsterLevel", values, "Monster");
			searchRequirements.add(levelRequirement);
		}
	}
	
	private void addHeroRequirements(List<Requirement> searchRequirements) {
		
	}
	
	private void addVillageRequirements(List<Requirement> searchRequirements) {
		
	}
	
	private void addAttributeRequirementIfChecked(int checkBoxId, String attributeName, String cardType,
			List<Requirement> searchRequirements) {
		CheckBox checkbox = (CheckBox)findViewById(checkBoxId);
		if(checkbox.isChecked()) {
			List<String> values = new ArrayList<String>();
			values.add(attributeName);
			Requirement requirement = Requirement.buildRequirement(attributeName, "HasAnyAttributes", values, cardType);
			searchRequirements.add(requirement);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_SEARCH_RESULT && resultCode == 1) {
			//pass the result back to the parent
			setResult(1, data);
	    	finish();
		}
	}
}
