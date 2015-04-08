package com.mgeske.tsgamebuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CardListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private int cardItemResource;
	private int cardNameResourceId;
	private int cardTypeResourceId;
	private int cardSetResourceId;
	private int headerItemResource;
	private int headerNameResourceId;
	private List<Object> items;
	private CardList cardList;
	
	public static final int HEADER_TYPE = 0;
	public static final int CARD_ITEM_TYPE = 1;
	
	public CardListAdapter(Context context, int cardItemResource, int cardNameResourceId, int cardTypeResourceId, int cardSetResourceId, 
			int headerItemResource, int headerNameResourceId, CardList cardList) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cardItemResource = cardItemResource;
		this.cardNameResourceId = cardNameResourceId;
		this.cardTypeResourceId = cardTypeResourceId;
		this.cardSetResourceId = cardSetResourceId;
		this.headerItemResource = headerItemResource;
		this.headerNameResourceId = headerNameResourceId;
		this.cardList = cardList;
		initItemList();
	}
	
	private void initItemList() {
		items = new ArrayList<Object>();
		addItems(R.string.dungeon_cards_header, cardList.getDungeonCards());
		addItems(R.string.guardian_cards_header, cardList.getGuardianCards());
		addItems(R.string.thunderstone_cards_header, cardList.getThunderstoneCards());
		addItems(R.string.hero_cards_header, cardList.getHeroCards());
		addItems(R.string.village_cards_header, cardList.getVillageCards());
	}
	
	private void addItems(int headerId, List<? extends Card> cards) {
		if(!cards.isEmpty()) {
			String header = context.getString(headerId);
			items.add(header);
			Collections.sort(cards);
			items.addAll(cards);
		}
	}
	
	public boolean addCard(Card card) {
		if(cardList.addCard(card, false)) {
			initItemList();
			notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	public void remove(int position) {
		Card card = (Card)getItem(position);
		cardList.removeCard(card);
		items.remove(position);
		if(getItemViewType(position-1) == HEADER_TYPE &&
				(items.size() == position || getItemViewType(position) == HEADER_TYPE)) {
			//this was the only item for this group - remove the header as well
			items.remove(position-1);
		}
		notifyDataSetChanged();
	}
	
	public CardList getCardList() {
		return cardList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Object item = getItem(position);
		int type = getItemViewType(position);
		

		if (convertView == null) {
			switch(type) {
				case HEADER_TYPE: convertView = inflater.inflate(headerItemResource, parent, false); break;
				case CARD_ITEM_TYPE: convertView = inflater.inflate(cardItemResource, parent, false); break;
			}
	    }
		
		switch(type) {
			case HEADER_TYPE: populateHeaderView((String)item, convertView, parent); break;
			case CARD_ITEM_TYPE: populateCardView((Card)item, convertView, parent); break;
		}
		return convertView;
	}
	
	private void populateHeaderView(String headerName, View view, ViewGroup parent) {
		TextView headerNameView = (TextView)view.findViewById(headerNameResourceId);
		headerNameView.setText(headerName);
	}
	
	private void populateCardView(Card card, View view, ViewGroup parent) {
		TextView cardNameView = (TextView)view.findViewById(cardNameResourceId);
		cardNameView.setText(card.getCardName());
		TextView cardTypeView = (TextView)view.findViewById(cardTypeResourceId);
		cardTypeView.setText(card.getCardSubtype());
		TextView cardSetView = (TextView)view.findViewById(cardSetResourceId);
		cardSetView.setText(card.getSetAbbreviation());
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		Object item = getItem(position);
		if(item instanceof String) {
			return HEADER_TYPE;
		} else {
			return CARD_ITEM_TYPE;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}
}


