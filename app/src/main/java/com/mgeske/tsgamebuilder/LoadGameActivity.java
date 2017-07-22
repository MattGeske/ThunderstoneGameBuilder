package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.List;

import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.db.CardDatabase;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class LoadGameActivity extends ActionBarActivity {
	private CardDatabase cardDb = null;
	private GameListPagerAdapter gameListPagerAdapter = null;
	private List<SavedGame> savedGameInfo = null;
	private List<SavedGame> filteredSavedGameInfo = null;
	private List<SavedGame> presetGameInfo = null;
	private List<SavedGame> filteredPresetGameInfo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_game);
		
		//get the game names
		cardDb = new CardDatabase(this);
		savedGameInfo = cardDb.getSavedGames("user");
		presetGameInfo = cardDb.getSavedGames("AEG");
		filteredSavedGameInfo = filterGames(savedGameInfo);
		filteredPresetGameInfo = filterGames(presetGameInfo);
		
		 
		//set up tabs
		ViewPager pager = (ViewPager)findViewById(R.id.game_list_pager);
		gameListPagerAdapter = new GameListPagerAdapter(getSupportFragmentManager(), this);
		gameListPagerAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SavedGame savedGame = (SavedGame)parent.getItemAtPosition(position);
				loadGame(savedGame);
			}
		});
		pager.setAdapter(gameListPagerAdapter);

		showGames(false);
		
		CheckBox showAll = (CheckBox)findViewById(R.id.show_all_saved_games);
		showAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				showGames(isChecked);
			}
		});
	}
	
	private List<SavedGame> filterGames(List<SavedGame> allGames) {
		String[] chosenSets = Util.getChosenSets(this);
		List<SavedGame> filteredGames = new ArrayList<SavedGame>();
		for(SavedGame savedGame : allGames) {
			boolean include = true;
			for(String setName : savedGame.getRequiredSetNamesArray()) {
				if(!Util.arrayContains(chosenSets, setName)) {
					include = false;
					break;
				}
			}
			if(include) {
				filteredGames.add(savedGame);
			}
		}
		return filteredGames;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cardDb.close();
		cardDb = null;
	}
	
	private void showGames(boolean showAll) {
		List<SavedGame> savedGamesToShow = showAll? savedGameInfo : filteredSavedGameInfo;
		List<SavedGame> presetGamesToShow = showAll? presetGameInfo : filteredPresetGameInfo;
		gameListPagerAdapter.setGameLists(savedGamesToShow, presetGamesToShow);
	}

	private void loadGame(SavedGame savedGame) {
		String gameName = savedGame.getGameName();
		CardList cardList = cardDb.loadSavedGame(gameName, savedGame.getGameSource());
		Intent data = new Intent(this, MainScreenActivity.class);
		data.putExtra("gameName", gameName);
		data.putExtra("cardList", cardList);
		setResult(1, data);
    	finish();
	}
}

class GameListPagerAdapter extends FragmentPagerAdapter {
	private ClickableListFragment savedGameListFragment;
	private SavedGameListAdapter savedGameListAdapter;
	private ClickableListFragment presetGameListFragment;
	private SavedGameListAdapter presetGameListAdapter;
	private AdapterView.OnItemClickListener mOnItemClickListener = null;
	
	public GameListPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		savedGameListFragment = new ClickableListFragment();
		savedGameListAdapter = new SavedGameListAdapter(context, R.layout.game_list_item, R.id.game_name, R.id.set_names);
		savedGameListFragment.setListAdapter(savedGameListAdapter);
		savedGameListFragment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				performItemClick(parent, view, position, id);
			}
		});
		
		presetGameListFragment = new ClickableListFragment();
		presetGameListAdapter = new SavedGameListAdapter(context, R.layout.game_list_item, R.id.game_name, R.id.set_names);
		presetGameListFragment.setListAdapter(presetGameListAdapter);
		presetGameListFragment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				performItemClick(parent, view, position, id);
			}
		});
	}
	
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

	public boolean performItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(parent, view, position, id);
            return true;
        }

        return false;
    }

	@Override
	public Fragment getItem(int position) {
		return position == 0 ? savedGameListFragment : presetGameListFragment;
	}

	@Override
	public int getCount() {
		return 2;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return position == 0 ? "Saved Games" : "Preset Games";
	}

	public void setGameLists(List<SavedGame> savedGameInfo, List<SavedGame> presetGameInfo) {
		savedGameListAdapter.setSavedGameList(savedGameInfo);
		presetGameListAdapter.setSavedGameList(presetGameInfo);
	}
}

class ClickableListFragment extends ListFragment {
	private AdapterView.OnItemClickListener mOnItemClickListener = null;
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				performItemClick(parent, view, position, id);
			}
		});
	}
	
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

	public boolean performItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(parent, view, position, id);
            return true;
        }

        return false;
    }
}
