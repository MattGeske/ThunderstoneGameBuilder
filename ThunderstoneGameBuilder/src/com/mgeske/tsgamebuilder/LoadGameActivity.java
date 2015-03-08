package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.db.CardDatabase;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

public class LoadGameActivity extends ActionBarActivity {
	private CardDatabase cardDb = null;
	private SavedGameListAdapter gameListAdapter = null;
	private List<SavedGame> gameInfo = null;
	private List<SavedGame> filteredGameInfo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_game);
		
		//get the game names
		cardDb = new CardDatabase(this);
		gameInfo = cardDb.getSavedGames();
		filteredGameInfo = new ArrayList<SavedGame>();
		for(SavedGame savedGame : gameInfo) {
			boolean include = true;
			for(String setName : savedGame.getRequiredSetNamesArray()) {
				if(!Util.arrayContains(Util.getChosenSets(this), setName)) {
					include = false;
					break;
				}
			}
			if(include) {
				filteredGameInfo.add(savedGame);
			}
		}
		 
		//set up ArrayAdapter
		gameListAdapter = new SavedGameListAdapter(this, R.layout.game_list_item, R.id.game_name, R.id.set_names);
		ListView gameListView = (ListView) findViewById(R.id.game_list);
		gameListView.setAdapter(gameListAdapter);
		gameListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView gameNameView = (TextView)view.findViewById(R.id.game_name);
				loadGame(gameNameView.getText().toString());
			}
			
		});
		showGames(false);
		
		CheckBox showAll = (CheckBox)findViewById(R.id.show_all_saved_games);
		showAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				showGames(isChecked);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cardDb.close();
		cardDb = null;
	}
	
	private void showGames(boolean showAll) {
		List<SavedGame> gamesToShow = showAll? gameInfo : filteredGameInfo;
		gameListAdapter.setSavedGameList(gamesToShow);
	}

	private void loadGame(String gameName) {
		CardList cardList = cardDb.loadSavedGame(gameName);
		Intent data = new Intent(this, MainScreenActivity.class);
		data.putExtra("gameName", gameName);
		data.putExtra("cardList", cardList);
		setResult(1, data);
    	finish();
	}
}
