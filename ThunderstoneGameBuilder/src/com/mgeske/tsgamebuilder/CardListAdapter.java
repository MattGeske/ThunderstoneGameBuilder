package com.mgeske.tsgamebuilder;

import java.util.logging.Logger;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CardListAdapter extends BaseAdapter {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private LayoutInflater inflater;
	private int cardItemResource;
	private int cardNameResourceId;
	private int cardTypeResourceId;
	private int cardSetResourceId;
	private int headerItemResource;
	private int headerNameResourceId;
	private CardList cardList;
	private int dungeonHeaderPosition;
	private int dungeonStartPosition;
	private int thunderstoneHeaderPosition;
	private int thunderstoneStartPosition;
	private int heroHeaderPosition;
	private int heroStartPosition;
	private int villageHeaderPosition;
	private int villageStartPosition;
	
	private static final String DUNGEON_HEADER = "DUNGEON";
	private static final String THUNDERSTONE_HEADER = "THUNDERSTONE";
	private static final String HERO_HEADER = "HERO";
	private static final String VILLAGE_HEADER = "VILLAGE";
	
	private static final int HEADER_TYPE = 0;
	private static final int CARD_ITEM_TYPE = 1;
	
	public CardListAdapter(Context context, int cardItemResource, int cardNameResourceId, int cardTypeResourceId, int cardSetResourceId, 
			int headerItemResource, int headerNameResourceId, CardList cardList) {
		this.inflater = LayoutInflater.from(context);
		this.cardItemResource = cardItemResource;
		this.cardNameResourceId = cardNameResourceId;
		this.cardTypeResourceId = cardTypeResourceId;
		this.cardSetResourceId = cardSetResourceId;
		this.headerItemResource = headerItemResource;
		this.headerNameResourceId = headerNameResourceId;
		this.cardList = cardList;
		

		this.dungeonHeaderPosition = 0;
		this.dungeonStartPosition = dungeonHeaderPosition+1;
		this.thunderstoneHeaderPosition = dungeonStartPosition+cardList.getDungeonCards().size();
		this.thunderstoneStartPosition = thunderstoneHeaderPosition+1;
		this.heroHeaderPosition = thunderstoneStartPosition+cardList.getThunderstoneCards().size();
		this.heroStartPosition = heroHeaderPosition+1;
		this.villageHeaderPosition = heroStartPosition+cardList.getHeroCards().size();
		this.villageStartPosition = villageHeaderPosition+1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Object item = getItem(position);
		int type = getItemViewType(position);
		

		logger.info("Called getView for position="+position+", item="+item+", itemType="+type+", convertView="+convertView+", parent="+parent);
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
		cardTypeView.setText(card.getCardType());
		TextView cardSetView = (TextView)view.findViewById(cardSetResourceId);
		cardSetView.setText(card.getSetName());
	}

	@Override
	public int getCount() {
		return cardList.getDungeonCards().size() + cardList.getThunderstoneCards().size() + cardList.getHeroCards().size() + cardList.getVillageCards().size() + 4;
	}

	@Override
	public Object getItem(int position) {
		/*
		 * Order: dungeon header, dungeon cards, thunderstone header, thunderstone cards, hero header, hero cards, village header, village cards
		 */
		if(position == dungeonHeaderPosition) {
			return DUNGEON_HEADER;
		} else if(position == thunderstoneHeaderPosition) {
			return THUNDERSTONE_HEADER;
		} else if(position == heroHeaderPosition) {
			return HERO_HEADER;
		} else if(position == villageHeaderPosition) {
			return VILLAGE_HEADER;
		} else if(position < thunderstoneHeaderPosition) {
			int index = position-dungeonStartPosition;
			return cardList.getDungeonCards().get(index);
		} else if(position < heroHeaderPosition) {
			int index = position-thunderstoneStartPosition;
			return cardList.getThunderstoneCards().get(index);
		} else if(position < villageHeaderPosition) {
			int index = position-heroStartPosition;
			return cardList.getHeroCards().get(index);
		} else {
			int index = position-villageStartPosition;
			return cardList.getVillageCards().get(index);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if(position == dungeonHeaderPosition || position == thunderstoneHeaderPosition || position == heroHeaderPosition || position == villageHeaderPosition) {
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


