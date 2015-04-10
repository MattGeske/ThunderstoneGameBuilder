package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SearchResultsActivity extends FragmentActivity {
	private List<Card> cardResults;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);
		Intent intent = getIntent();
		cardResults = intent.getParcelableArrayListExtra("cardResults");
		
		SearchResultAdapter adapter = new SearchResultAdapter(this, R.layout.search_results_list_item, R.id.card_name,
				R.id.card_type, cardResults);
		
		ListView lv = (ListView)findViewById(R.id.search_result_list);
		lv.setItemsCanFocus(false);
		lv.setAdapter(adapter);
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Card card = (Card)parent.getItemAtPosition(position);
				viewCardDetails(card);
				return true;
			}
		});
		
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
    
    private void viewCardDetails(Card card) {
		CardInformationDialog dialog = CardInformationDialog.getCardInformationDialog(card);
		dialog.show(getSupportFragmentManager(), "CardInformationDialog");
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
