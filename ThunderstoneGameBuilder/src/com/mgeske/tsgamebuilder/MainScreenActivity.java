package com.mgeske.tsgamebuilder;

import java.util.ArrayList;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.randomizer.IRandomizer;
import com.mgeske.tsgamebuilder.randomizer.PresetRandomizer;
import com.mgeske.tsgamebuilder.randomizer.SmartRandomizer;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;



public class MainScreenActivity extends ActionBarActivity {
	private IRandomizer randomizer = new SmartRandomizer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void buildNewGame(View view) {
    	CardList cardList = randomizer.generateCardList(this);
    	Intent intent = new Intent(this, ShowGameActivity.class);
    	ArrayList<String> dungeonCardNames = new ArrayList<String>();
    	for(DungeonCard c : cardList.getDungeonCards()) {
    		String cardName = c.getCardName();
    		if(c.getLevel() != null) {
    			cardName = cardName+" (Level "+c.getLevel()+")";
    		}
    		dungeonCardNames.add(cardName);
    	}
    	ArrayList<String> thunderstoneCardNames = new ArrayList<String>();
    	for(Card c : cardList.getThunderstoneCards()) {
    		thunderstoneCardNames.add(c.getCardName());
    	}
    	ArrayList<String> heroCardNames = new ArrayList<String>();
    	for(Card c : cardList.getHeroCards()) {
    		heroCardNames.add(c.getCardName());
    	}
    	ArrayList<String> villageCardNames = new ArrayList<String>();
    	for(Card c : cardList.getVillageCards()) {
    		villageCardNames.add(c.getCardName());
    	}
    	intent.putStringArrayListExtra("com.mgeske.tsgamebuilder.dungeonCardList", dungeonCardNames);
    	intent.putStringArrayListExtra("com.mgeske.tsgamebuilder.thunderstoneCardList", thunderstoneCardNames);
    	intent.putStringArrayListExtra("com.mgeske.tsgamebuilder.heroCardList", heroCardNames);
    	intent.putStringArrayListExtra("com.mgeske.tsgamebuilder.villageCardList", villageCardNames);
    	
//    	CardDatabase mDb = new CardDatabase(this);
//    	ArrayList<String> heroCardNames = new ArrayList<String>();
//    	Cursor c = mDb.getHeroes();
//    	while(c.moveToNext()) {
//    		String cardName = c.getString(c.getColumnIndexOrThrow("cardName"));
//    		heroCardNames.add(cardName);
//    	}
//    	c.close();
//    	Intent intent = new Intent(this, ShowGameActivity.class);
//    	intent.putStringArrayListExtra("com.mgeske.tsgamebuilder.dungeonCardList", new ArrayList<String>());
//    	intent.putStringArrayListExtra("com.mgeske.tsgamebuilder.heroCardList", heroCardNames);
//    	intent.putStringArrayListExtra("com.mgeske.tsgamebuilder.villageCardList", new ArrayList<String>());
    	
    	startActivity(intent);
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
