package com.mgeske.tsgamebuilder;


import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.randomizer.IRandomizer;
import com.mgeske.tsgamebuilder.randomizer.SmartRandomizer;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;



public class MainScreenActivity extends ActionBarActivity {
	private IRandomizer randomizer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(preferences.getBoolean("keep_screen_on", false)) {
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    	int num_monster = getIntPreference(preferences, "num_monster", R.string.default_num_monsters);
    	int num_thunderstone = getIntPreference(preferences, "num_thunderstone", R.string.default_num_thunderstone);
    	int num_hero = getIntPreference(preferences, "num_hero", R.string.default_num_hero);
    	int num_village = getIntPreference(preferences, "num_village", R.string.default_num_village);
    	boolean village_limits = getBooleanPreference(preferences, "village_limits", R.string.default_village_limits);
    	boolean monster_levels = getBooleanPreference(preferences, "monster_levels", R.string.default_monster_levels);
        if(randomizer == null) {
        	randomizer = new SmartRandomizer(num_monster, num_thunderstone, num_hero, num_village, village_limits, monster_levels);
        } else {
        	randomizer.setLimits(num_monster, num_thunderstone, num_hero, num_village, village_limits, monster_levels);
        }
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.action_settings: openSettings(); return true;
			case R.id.action_newgame: buildNewGame(); return true;
		}
        return super.onOptionsItemSelected(item);
    }
    
    private void openSettings() {
    	Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent); 
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
