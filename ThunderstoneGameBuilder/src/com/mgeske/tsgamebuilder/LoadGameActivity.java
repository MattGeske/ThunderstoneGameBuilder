package com.mgeske.tsgamebuilder;

import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.db.CardDatabase;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LoadGameActivity extends ActionBarActivity {
	private CardDatabase cardDb = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_game);
		 
		String[] gameNames;
		cardDb = new CardDatabase(this);
		gameNames = cardDb.getSavedGames();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.game_list_item, R.id.game_name);
		for(String gameName : gameNames) {
			adapter.add(gameName);
		}
		
		ListView gameListView = (ListView) findViewById(R.id.game_list);
		gameListView.setAdapter(adapter);
		gameListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView gameNameView = (TextView)view.findViewById(R.id.game_name);
				loadGame(gameNameView.getText().toString());
			}
			
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cardDb.close();
		cardDb = null;
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
