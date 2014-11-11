package com.mgeske.tsgamebuilder;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CardListAdapter extends ArrayAdapter<String> {
	
	public CardListAdapter(Context context, int resource, List<String> monsters, List<String> heroes, List<String> village) {
		super(context, resource);
		add("MONSTERS");
		for(String m : monsters) {
			add(m);
		}
		add("");
		add("HEROES");
		for(String h : heroes) {
			add(h);
		}
		add("");
		add((String) "VILLAGE");
		for(String v : village) {
			add(v);
		}
	}

//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		String item = getItem(position);
//		if(item == "") {
//			TextView t = new TextView(getContext());
//			t.setClickable(false);
//			return t;
//		} else {
//			return super.getView(position, convertView, parent);
//		}
//	}
	
	

}
