package com.mgeske.tsgamebuilder;


import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.randomizer.IRandomizer;
import com.mgeske.tsgamebuilder.randomizer.SmartRandomizer;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;



public class MainScreenActivity extends ActionBarActivity {
	private IRandomizer randomizer = new SmartRandomizer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.action_settings: return true;
			case R.id.action_newgame: buildNewGame(); return true;
		}
        return super.onOptionsItemSelected(item);
    }
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
	}


	public void buildNewGame(View view) {
    	buildNewGame();
    }
    
    private void buildNewGame() {
    	CardList cardList = randomizer.generateCardList(this);
		ListView lv = (ListView) findViewById(R.id.card_list);
		lv.setAdapter(new CardListAdapter(this, R.layout.card_list_item, R.id.card_name, R.id.card_type, R.id.card_set, R.layout.card_header_item, R.id.header_name, cardList));
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_screen, container, false);
            return rootView;
        }
    }
}
