package com.mgeske.tsgamebuilder;

import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class SearchResultAdapter extends ArrayAdapter<Card> {
	private LayoutInflater inflater;
	private int cardItemResource;
	private int cardNameResourceId;
	private int cardTypeResourceId;

	public SearchResultAdapter(Context context, int resource, int cardNameResourceId, int cardTypeResourceId,
			List<Card> objects) {
		super(context, resource, objects);
		this.inflater = LayoutInflater.from(context);
		this.cardItemResource = resource;
		this.cardNameResourceId = cardNameResourceId;
		this.cardTypeResourceId = cardTypeResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Card card = getItem(position);
		
		if (convertView == null) {
			convertView = inflater.inflate(cardItemResource, parent, false);
		}
		TextView cardNameView = (TextView)convertView.findViewById(cardNameResourceId);
		cardNameView.setText(card.getCardName());
		
		TextView cardTypeView = (TextView)convertView.findViewById(cardTypeResourceId);
		String subType = card.getCardSubtype();
		if(subType != null && !"".equals(subType)) {
			cardTypeView.setText(subType);
		} else {
			cardTypeView.setVisibility(View.GONE);
			LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 80);
			cardNameView.setLayoutParams(params);
		}
		
		return convertView;
	}
}
