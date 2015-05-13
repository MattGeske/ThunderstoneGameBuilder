package com.mgeske.tsgamebuilder;


import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.db.CardDatabase;
import com.mgeske.tsgamebuilder.randomizer.SmartRandomizer;

import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainScreenActivity extends ActionBarActivity {
	private final int REQUEST_CODE_LOAD_GAME = 1;
	private final int REQUEST_SEARCH_CARD = 2;
	private final int CONTEXT_MENU_REMOVE_CARD = 0;
	private final int CONTEXT_MENU_REPLACE_CARD = 1;
	private final int CONTEXT_MENU_CARD_DETAILS = 2;
	private SmartRandomizer randomizer = null;
	private CardDatabase cardDb = null;
	private Menu actionMenu = null;
	private boolean userHasChosenSets = true;
	private ListView cardListView = null;
	private CardListAdapter cardListAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        findViewById(R.id.choose_sets_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openChooseSets();
			}
        });
        

        CardList cardList = new CardList(null, null);
        cardListAdapter = new CardListAdapter(this, R.layout.card_list_item, R.id.card_name, R.id.card_type, R.id.card_set, R.layout.card_header_item, R.id.header_name, cardList);
        cardListAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				chooseView();
			}
		});
        
        cardListView = (ListView)findViewById(R.id.card_list);
        cardListView.setAdapter(cardListAdapter);
        cardListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				viewCardDetails(position);
			}
        });
        registerForContextMenu(cardListView);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	if(v.getId() == R.id.card_list) {
    		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
    		int type = cardListAdapter.getItemViewType(info.position);
    		if(type == CardListAdapter.HEADER_TYPE) {
    			return;
    		}
    		Card card = (Card)cardListAdapter.getItem(info.position);
    		menu.setHeaderTitle(card.getCardName());
    		menu.add(Menu.NONE, CONTEXT_MENU_CARD_DETAILS, 0, "View card details");
    		menu.add(Menu.NONE, CONTEXT_MENU_REMOVE_CARD, 1, "Remove card");
    		menu.add(Menu.NONE, CONTEXT_MENU_REPLACE_CARD, 2, "Replace with random card");
    	}
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	if(item.getItemId() == CONTEXT_MENU_CARD_DETAILS) {
    		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    	viewCardDetails(info.position);
    	} else if(item.getItemId() == CONTEXT_MENU_REMOVE_CARD) {
	    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    	removeCard(info.position);
    	} else if(item.getItemId() == CONTEXT_MENU_REPLACE_CARD) {
    		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    	replaceCard(info.position);
    	}
    	return true;
    }
    
	@Override
    protected void onResume() {
    	super.onResume();
        String[] chosenSets = Util.getChosenSets(this);
    	userHasChosenSets = (chosenSets.length > 0);
    	if(actionMenu != null) {
    		actionMenu.setGroupEnabled(R.id.set_dependent_actions, userHasChosenSets);
    	}

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getBoolean("keep_screen_on", false)) {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        
    	if(cardDb == null) {
    		cardDb = new CardDatabase(this);
    	}
        if(randomizer == null) {
        	randomizer = new SmartRandomizer(cardDb);
        }
        chooseView();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_CODE_LOAD_GAME && resultCode == 1) {
			CardList cardList = data.getParcelableExtra("cardList");
			displayGame(cardList);
		} else if(requestCode == REQUEST_SEARCH_CARD && resultCode == 1) {
			ArrayList<Card> cards = data.getParcelableArrayListExtra("cardsToAdd");
			
			List<String> cardNames = new ArrayList<String>();
			for(Card card : cards) {
				if(cardListAdapter.addCard(card)) {
					cardNames.add(card.getCardName());
				}
			}

			if(cardNames.size() > 0) {
				String plural = (cardNames.size() > 1)? "s" : "";
				String message = "Added card"+plural+" '"+TextUtils.join("', '", cardNames)+"'";
				Toast t = Toast.makeText(this, message, Toast.LENGTH_LONG);
				t.show();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cardDb.close();
		cardDb = null;
	}
    
    private int getIntPreference(SharedPreferences preferences, String key, int default_id) {
    	String default_string_value = getString(default_id);
    	return Integer.parseInt(preferences.getString(key, default_string_value));
    }
    
    private boolean getBooleanPreference(SharedPreferences preferences, String key, int default_id) {
    	String default_string_value = getString(default_id);
    	boolean default_value = Boolean.parseBoolean(default_string_value);
    	return preferences.getBoolean(key, default_value);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        actionMenu = menu;
        actionMenu.setGroupEnabled(R.id.set_dependent_actions, userHasChosenSets);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.action_settings: openSettings(); return true;
			case R.id.action_choosesets: openChooseSets(); return true;
			case R.id.action_newgame: buildNewGame(); return true;
			case R.id.action_loadgame: loadGame(); return true;
			case R.id.action_addcard: showAddCardContext(); return true;
			case R.id.action_searchcard: showSearchCardActivity(); return true;
		}
        return super.onOptionsItemSelected(item);
    }
    
    private void openSettings() {
    	Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent); 
	}
    
    private void openChooseSets() {
    	Intent intent = new Intent(this, ChooseSetsActivity.class);
    	startActivity(intent);
    }
    
    private void buildNewGame() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	int num_monster = getIntPreference(preferences, "num_monster", R.string.default_num_monsters);
    	int num_thunderstone = getIntPreference(preferences, "num_thunderstone", R.string.default_num_thunderstone);
    	int num_hero = getIntPreference(preferences, "num_hero", R.string.default_num_hero);
    	int num_village = getIntPreference(preferences, "num_village", R.string.default_num_village);
    	boolean village_limits = getBooleanPreference(preferences, "village_limits", R.string.default_village_limits);
    	boolean monster_levels = getBooleanPreference(preferences, "monster_levels", R.string.default_monster_levels);
    	CardList cardList = randomizer.generateCardList(num_monster, num_thunderstone, num_hero, num_village, village_limits, monster_levels);
		displayGame(cardList);
    }
    
    private void displayGame(CardList cardList) {
    	cardListAdapter.setCardList(cardList);
    }
    
    private void loadGame() {
    	Intent intent = new Intent(this, LoadGameActivity.class);
    	startActivityForResult(intent, REQUEST_CODE_LOAD_GAME);
    }
    
    private void viewCardDetails(int position) {
		int type = cardListAdapter.getItemViewType(position);
		if(type == CardListAdapter.CARD_ITEM_TYPE) {
			Card card = (Card)cardListAdapter.getItem(position);
			CardInformationDialog dialog = CardInformationDialog.getCardInformationDialog(card);
			dialog.show(getSupportFragmentManager(), "CardInformationDialog");
		}
    }
    
    private void chooseView() {
    	//if the user has not chosen any expansions yet, show the Choose Expansions button
    	//if the user has loaded/generated/created a game, show the card list
    	//otherwise show the help info
    	View chosenView;
    	View[] viewChoices = {
    			findViewById(R.id.choose_sets_button),
    			findViewById(R.id.card_list),
    			findViewById(R.id.help_info),
    	};
    	if(!userHasChosenSets) {
    		chosenView = viewChoices[0];
    	} else if(this.cardListAdapter.getCount() > 0) {
    		chosenView = viewChoices[1];
    	} else {
    		chosenView = viewChoices[2];
    	}
    	
    	for(View view : viewChoices) {
    		view.setVisibility(View.INVISIBLE);
    	}
    	chosenView.setVisibility(View.VISIBLE);
    }
    
    private void showAddCardContext() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Add card...");
    	final String[] cardTypes = {"Monster", "Treasure", "Trap", "Guardian", "Thunderstone", "Hero", "Village"};
    	CharSequence[] dialogChoices = new CharSequence[cardTypes.length];
    	for(int i = 0; i < cardTypes.length; i++) {
    		String cardType = cardTypes[i];
    		dialogChoices[i] = "Random "+cardType+" card";
    	}
    	builder.setItems(dialogChoices, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String cardType = cardTypes[which];
				addRandomCard(cardType);
			}
    	});
    	builder.show();
    }
    
    private void addRandomCard(String cardType) {
    	CardList currentCards = cardListAdapter.getCardList();
		Card newCard = randomizer.getRandomCard(currentCards, cardType);
		String toastText;
		if(newCard == null) {
			toastText = "No remaining "+cardType+" cards to add";
		} else {
			cardListAdapter.addCard(newCard);
			toastText = "Added card '"+newCard.getCardName()+"'";
		}
		Toast t = Toast.makeText(this, toastText, Toast.LENGTH_LONG);
		t.show();
    }
    
    private void removeCard(int position) {
    	cardListAdapter.remove(position);
    }
    
    private void replaceCard(int position) {
    	Card removedCard = (Card)cardListAdapter.getItem(position);
    	String cardType = removedCard.getCardType();
    	removeCard(position);
    	addRandomCard(cardType);
    }
    
    private void showSearchCardActivity() {
    	Intent intent = new Intent(this, SearchCardActivity.class);
    	startActivityForResult(intent, REQUEST_SEARCH_CARD);
    }
}
