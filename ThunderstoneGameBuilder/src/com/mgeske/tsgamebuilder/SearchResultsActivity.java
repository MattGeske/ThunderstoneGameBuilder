package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SearchResultsActivity extends Activity {
	private List<Card> cardResults;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);
		Intent intent = getIntent();
		cardResults = intent.getParcelableArrayListExtra("cardResults");
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice);
		for(Card card : cardResults) {
			adapter.add(card.getCardName());
		}
		
		ListView lv = (ListView)findViewById(R.id.search_result_list);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lv.setAdapter(adapter);
		
		Button modifySearchButton = (Button)findViewById(R.id.results_modify_search_button);
		modifySearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		Button addCardsButton = (Button)findViewById(R.id.results_add_button);
		addCardsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				returnData();
			}
		});
	}
	
	private void returnData() {
		ListView lv = (ListView)findViewById(R.id.search_result_list);
		ListAdapter adapter = lv.getAdapter();
		SparseBooleanArray checkedItemPositions = lv.getCheckedItemPositions();
		
		ArrayList<Card> cardsToAdd = new ArrayList<Card>();
		for(int position = 0; position < adapter.getCount(); position++) {
			if(checkedItemPositions.get(position)) {
				Card card = cardResults.get(position);
				cardsToAdd.add(card);
			}
		}
		
		Intent data = new Intent(this, MainScreenActivity.class);
		data.putParcelableArrayListExtra("cardsToAdd", cardsToAdd);
		setResult(1, data);
		finish();
	}

}
