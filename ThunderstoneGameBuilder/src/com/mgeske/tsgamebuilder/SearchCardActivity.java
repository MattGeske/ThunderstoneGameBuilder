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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SearchCardActivity extends Activity {
	private static final int REQUEST_CODE_SEARCH_RESULT = 1;
	private Map<String,View> searchFieldsMap = new HashMap<String,View>();
	private View currentShownView = null;
	private Spinner villageClassSpinner = null;
	private ArrayAdapter<String> villageClassSpinnerAdapter = null;
	private String[] villageClassSpinnerEntries = {""};
	private List<String> villageCardClasses = new ArrayList<String>();

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
		
		villageClassSpinner = (Spinner)findViewById(R.id.search_village_class_spinner);
		villageClassSpinner.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP) {
					openVillageClassSelector();
					return true;
				}
				return false;
			}
		});
		villageClassSpinner.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					openVillageClassSelector();
					return true;
				}
				return false;
			}
		});
		villageClassSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, villageClassSpinnerEntries);
		villageClassSpinner.setAdapter(villageClassSpinnerAdapter);
		updateVillageClassSpinner();
		
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
	
	private void openVillageClassSelector() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final String[] items = this.getResources().getStringArray(R.array.village_classes);
		boolean[] selected = new boolean[items.length];
		for(int i = 0; i < items.length; i++) {
			String cardClass = items[i];
			selected[i] = villageCardClasses.contains(cardClass);
		}
		builder.setMultiChoiceItems(items, selected, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				String cardClass = items[which];
				if(isChecked) {
					addVillageClass(cardClass);
				} else {
					removeVillageClass(cardClass);
				}
			}
		});
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				updateVillageClassSpinner();
			}
		});
		builder.show();
	}
	
	private void addVillageClass(String cardClass) {
		villageCardClasses.add(cardClass);
	}
	
	private void removeVillageClass(String cardClass) {
		villageCardClasses.remove(cardClass);
	}
	
	private void updateVillageClassSpinner() {
		String newText;
		if(villageCardClasses.size() == 0) {
			newText = "Any Class";
		} else {
			Collections.sort(villageCardClasses);
			newText = "Class: "+TextUtils.join(", ", villageCardClasses);
		}
		villageClassSpinnerEntries[0] = newText;
		villageClassSpinnerAdapter.notifyDataSetChanged();
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
		if(!"".equals(cardSearchText)) {
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
		String monsterLevel = ((String)monsterLevelSpinner.getSelectedItem()).replaceAll("[^\\d]", "");
		if(monsterLevelSpinner.getSelectedItemPosition() > 0) {
			List<String> values = new ArrayList<String>();
			values.add(monsterLevel);
			Requirement levelRequirement = Requirement.buildRequirement("MonsterLevel", "MonsterLevel", values, "Monster");
			searchRequirements.add(levelRequirement);
		}
	}
	
	private void addHeroRequirements(List<Requirement> searchRequirements) {
		//order of most specific to least specific: removes disease, provides light, magic attack, class, race, physical attack, strength
		addAttributeRequirementIfChecked(R.id.search_hero_removes_disease, "REMOVES_DISEASE", "Hero", searchRequirements);
		addAttributeRequirementIfChecked(R.id.search_hero_light, "HAS_LIGHT", "Hero", searchRequirements);
		addAttributeRequirementIfChecked(R.id.search_hero_magic_attack, "HAS_MAGIC_ATTACK", "Hero", searchRequirements);
		
		Spinner heroClassSpinner = (Spinner)findViewById(R.id.search_hero_class);
		if(heroClassSpinner.getSelectedItemPosition() > 0) {
			String heroClass = (String)heroClassSpinner.getSelectedItem();
			List<String> values = new ArrayList<String>();
			values.add(heroClass);
			Requirement heroClassRequirement = Requirement.buildRequirement("heroClass", "HasAllClasses", values, "Hero");
			searchRequirements.add(heroClassRequirement);
		}
		Spinner heroRaceSpinner = (Spinner)findViewById(R.id.search_hero_race);
		if(heroRaceSpinner.getSelectedItemPosition() > 0) {
			String heroRace = (String)heroRaceSpinner.getSelectedItem();
			List<String> values = new ArrayList<String>();
			values.add(heroRace);
			Requirement heroRaceRequirement = Requirement.buildRequirement("heroRace", "HasRace", values, "Hero");
			searchRequirements.add(heroRaceRequirement);
		}

		addAttributeRequirementIfChecked(R.id.search_hero_physical_attack, "HAS_PHYSICAL_ATTACK", "Hero", searchRequirements);
		
		EditText strengthSearchField = (EditText)findViewById(R.id.search_hero_strength);
		String strengthSearchText = strengthSearchField.getText().toString().trim();
		if(!"".equals(strengthSearchText)) {
			List<String> values = new ArrayList<String>();
			values.add(strengthSearchText);
			Requirement heroStrengthRequirement = Requirement.buildRequirement("heroStrength", "HasStrength", values, "Hero");
			searchRequirements.add(heroStrengthRequirement);
		}
	}
	
	private void addVillageRequirements(List<Requirement> searchRequirements) {
		//order of most specific to least specific: removes disease, gives disease, provides additional buys, weight,
		//	strength, destroys cards, provides light, provides magic attack, cost, class, value, provides physical attack
		addAttributeRequirementIfChecked(R.id.search_village_removes_disease, "REMOVES_DISEASE", "Village", searchRequirements);
		addAttributeRequirementIfChecked(R.id.search_village_disease, "GIVES_DISEASE", "Village", searchRequirements);
		addAttributeRequirementIfChecked(R.id.search_village_additional_buys, "HAS_ADDITIONAL_BUY", "Village", searchRequirements);
		
		EditText weightSearchField = (EditText)findViewById(R.id.search_village_weight);
		String weightSearchText = weightSearchField.getText().toString().trim();
		if(!"".equals(weightSearchText)) {
			List<String> values = new ArrayList<String>();
			values.add(weightSearchText);
			Requirement weightRequirement = Requirement.buildRequirement("HasWeight", "HasWeight", values, "Village");
			searchRequirements.add(weightRequirement);
		}
		
		addAttributeRequirementIfChecked(R.id.search_village_strength, "HAS_STRENGTH", "Village", searchRequirements);
		addAttributeRequirementIfChecked(R.id.search_village_destroys_cards, "DESTROYS_CARDS", "Village", searchRequirements);
		addAttributeRequirementIfChecked(R.id.search_village_light, "HAS_LIGHT", "Village", searchRequirements);
		addAttributeRequirementIfChecked(R.id.search_village_magic_attack, "HAS_MAGIC_ATTACK", "Village", searchRequirements);
		
		EditText costSearchField = (EditText)findViewById(R.id.search_village_cost);
		String costSearchText = costSearchField.getText().toString().trim();
		if(!"".equals(costSearchText)) {
			List<String> values = new ArrayList<String>();
			values.add(costSearchText);
			Requirement costRequirement = Requirement.buildRequirement("CardCost", "CardCost", values, "Village");
			searchRequirements.add(costRequirement);
		}
		
		if(villageCardClasses.size() > 0) {
			Requirement classRequirement = Requirement.buildRequirement("villageClass", "HasAllClasses", villageCardClasses, "Village");
			searchRequirements.add(classRequirement);
		}
		
		EditText valueSearchField = (EditText)findViewById(R.id.search_village_value);
		String valueSearchText = valueSearchField.getText().toString().trim();
		if(!"".equals(valueSearchText)) {
			List<String> values = new ArrayList<String>();
			values.add(valueSearchText);
			Requirement valueRequirement = Requirement.buildRequirement("CardValue", "CardValue", values, "Village");
			searchRequirements.add(valueRequirement);
		}

		addAttributeRequirementIfChecked(R.id.search_village_physical_attack, "HAS_PHYSICAL_ATTACK", "Village", searchRequirements);
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
