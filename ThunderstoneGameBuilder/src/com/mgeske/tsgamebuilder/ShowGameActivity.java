package com.mgeske.tsgamebuilder;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ShowGameActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		List<String> dungeonList = intent.getStringArrayListExtra("com.mgeske.tsgamebuilder.dungeonCardList");
		List<String> thunderstoneList = intent.getStringArrayListExtra("com.mgeske.tsgamebuilder.thunderstoneCardList");
		List<String> heroList = intent.getStringArrayListExtra("com.mgeske.tsgamebuilder.heroCardList");
		List<String> villageList = intent.getStringArrayListExtra("com.mgeske.tsgamebuilder.villageCardList");
//		setContentView(R.layout.activity_show_game);
		CardListAdapter cAdapter = new CardListAdapter(this, android.R.layout.simple_list_item_1, dungeonList, thunderstoneList, heroList, villageList);
        setListAdapter(cAdapter);


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_game, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
