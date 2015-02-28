package com.mgeske.tsgamebuilder;

import java.util.logging.Logger;

import com.mgeske.tsgamebuilder.db.CardDatabase;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LoadGameActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_game);
		 
		CardDatabase cardDb = null;
		String[] gameNames;
		try {
			cardDb = new CardDatabase(this);
			gameNames = cardDb.getSavedGames();
		} finally {
			if(cardDb != null) {
				cardDb.close();
			}
		}
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
				loadGame(gameNameView.getText());
			}
			
		});
	}

	private void loadGame(CharSequence gameName) {
		Intent data = new Intent(this, MainScreenActivity.class);
		data.putExtra("gameName", gameName);
		setResult(1, data);
    	finish();
	}
}
