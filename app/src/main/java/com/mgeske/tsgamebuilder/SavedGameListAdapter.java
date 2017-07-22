package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SavedGameListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private int gameItemResource;
	private int gameNameResourceId;
	private int setNamesResourceId;
	private List<SavedGame> gameInfo;
	
	public SavedGameListAdapter(Context context, int gameItemResource, int gameNameResourceId, int setNamesResourceId) {
		this.inflater = LayoutInflater.from(context);
		this.gameItemResource = gameItemResource;
		this.gameNameResourceId = gameNameResourceId;
		this.setNamesResourceId = setNamesResourceId;
		this.gameInfo = new ArrayList<SavedGame>();
	}
	
	public void setSavedGameList(List<SavedGame> newGameInfo) {
		gameInfo.clear();
		gameInfo.addAll(newGameInfo);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return gameInfo.size();
	}

	@Override
	public Object getItem(int position) {
		return gameInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SavedGame savedGame = (SavedGame)getItem(position);
		if (convertView == null) {
			convertView = inflater.inflate(gameItemResource, parent, false);
		}
		TextView gameNameView = (TextView)convertView.findViewById(gameNameResourceId);
		gameNameView.setText(savedGame.getGameName());
		TextView setNamesView = (TextView)convertView.findViewById(setNamesResourceId);
		setNamesView.setText("Requires: "+savedGame.getRequiredSetNamesString());
		return convertView;
	}

}
