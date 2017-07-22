package com.mgeske.tsgamebuilder;

import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.GuardianCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public abstract class CardInformationDialog extends DialogFragment {
	private Card card;
	private LayoutInflater inflater;
	
	public static CardInformationDialog getCardInformationDialog(Card card) {
		if(card instanceof DungeonCard) {
			return new DungeonCardInformationDialog((DungeonCard)card);
		} else if(card instanceof GuardianCard) {
			return new GuardianCardInformationDialog((GuardianCard)card);
		} else if(card instanceof ThunderstoneCard) {
			return new ThunderstoneCardInformationDialog((ThunderstoneCard)card);
		} else if(card instanceof HeroCard) {
			return new HeroCardInformationDialog((HeroCard)card);
		} else if(card instanceof VillageCard) {
			return new VillageCardInformationDialog((VillageCard)card);
		} else {
			throw new RuntimeException("Couldn't find appropriate CardInformationDialog subclass for card "+card);
		}
	}
	
	protected CardInformationDialog(Card card) {
		this.card = card;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		inflater = LayoutInflater.from(getActivity());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View customView = getCardView();
		builder.setView(customView)
			   .setPositiveButton("Close", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
		return builder.create();
	}

	@SuppressLint("InflateParams")
	private View getCardView() {
		View cardView = inflater.inflate(R.layout.card_detail_view, null);

		//Card name
		setText(cardView, R.id.card_name, card.getCardName());
		
		//Subheader
		String subheaderText = getSubheaderText();
		if(subheaderText == null || "".equals(subheaderText)) {
			cardView.findViewById(R.id.subheader).setVisibility(View.GONE);
		} else {
			setText(cardView, R.id.subheader, subheaderText);
		}
		
		//Gives disease
		if(card.getAttributes().contains("GIVES_DISEASE")) {
			cardView.findViewById(R.id.gives_disease).setVisibility(View.VISIBLE);
			cardView.findViewById(R.id.separator_after_attributes).setVisibility(View.VISIBLE);
		}
		
		//Has light
		if(card.getAttributes().contains("HAS_LIGHT")) {
			cardView.findViewById(R.id.provides_light).setVisibility(View.VISIBLE);
			cardView.findViewById(R.id.separator_after_attributes).setVisibility(View.VISIBLE);
		}
		
		addCardTypeSpecificAttributes(cardView);
		
		//Card text
		String cardText = card.getCardText();
		if(cardText == null || "".equals(cardText)) {
			cardView.findViewById(R.id.card_text).setVisibility(View.GONE);
			cardView.findViewById(R.id.separator_after_text).setVisibility(View.GONE);
		} else {
			setText(cardView, R.id.card_text, cardText);
		}
		
		//thunderstone set
		String setName = "From: "+ card.getSetName();
		setText(cardView, R.id.set_name, setName);
		return cardView;
	}
	
	protected abstract String getSubheaderText();
	protected void addCardTypeSpecificAttributes(View cardView) {}
	
	protected void setText(View cardView, int textViewId, String text) {
		View view = cardView.findViewById(textViewId);
		view.toString();
		TextView tv = (TextView)cardView.findViewById(textViewId);
		tv.setText(text);
		tv.setVisibility(View.VISIBLE);
	}
}

class DungeonCardInformationDialog extends CardInformationDialog {
	private DungeonCard card;

	public DungeonCardInformationDialog(DungeonCard card) {
		super(card);
		this.card = card;
	}
	
	@Override
	protected String getSubheaderText() {
		String subheaderText = card.getCardType();
		if(subheaderText.startsWith("Monster")) {
			subheaderText += " - Level "+card.getLevel();
		}
		return subheaderText;
	}
	
	@Override
	protected void addCardTypeSpecificAttributes(View cardView) {
		//Ambusher icon
		if(card.getAttributes().contains("HAS_AMBUSHER")) {
			cardView.findViewById(R.id.ambusher_icon).setVisibility(View.VISIBLE);
			cardView.findViewById(R.id.separator_after_attributes).setVisibility(View.VISIBLE);
		}
		
		//Doomladen icon
		if(card.getAttributes().contains("HAS_DOOMLADEN")) {
			cardView.findViewById(R.id.doomladen_icon).setVisibility(View.VISIBLE);
			cardView.findViewById(R.id.separator_after_attributes).setVisibility(View.VISIBLE);
		}
	}
}

class GuardianCardInformationDialog extends CardInformationDialog {
	public GuardianCardInformationDialog(GuardianCard card) {
		super(card);
	}
	
	@Override
	protected String getSubheaderText() {
		return "Guardian";
	}
}

class ThunderstoneCardInformationDialog extends CardInformationDialog {
	private ThunderstoneCard card;
	
	public ThunderstoneCardInformationDialog(ThunderstoneCard card) {
		super(card);
		this.card = card;
	}
	
	@Override
	protected String getSubheaderText() {
		return card.getThunderstoneType();
	}
}

class HeroCardInformationDialog extends CardInformationDialog {
	private HeroCard card;
	
	public HeroCardInformationDialog(HeroCard card) {
		super(card);
		this.card = card;
	}
	
	@Override
	protected String getSubheaderText() {
		return TextUtils.join(" · ", card.getClasses())+"\n"+card.getRace();
	}
	
	@Override
	protected void addCardTypeSpecificAttributes(View cardView) {
		setText(cardView, R.id.strength, "Strength: "+card.getStrength());
		cardView.findViewById(R.id.separator_after_attributes).setVisibility(View.VISIBLE);
	}
}

class VillageCardInformationDialog extends CardInformationDialog {
	private VillageCard card;
	
	public VillageCardInformationDialog(VillageCard card) {
		super(card);
		this.card = card;
	}
	
	@Override
	protected String getSubheaderText() {
		return TextUtils.join(" · ", card.getClasses());
	}
	
	@Override
	protected void addCardTypeSpecificAttributes(View cardView) {
		setText(cardView, R.id.gold_cost, "Gold Cost: "+card.getCost());
		if(card.getValue() != null) {
			setText(cardView, R.id.gold_value, "Gold Value: "+card.getValue());
		}
		if(card.getWeight() != null) {
			setText(cardView, R.id.weight, "Weight: "+card.getWeight());
		}
		cardView.findViewById(R.id.separator_after_attributes).setVisibility(View.VISIBLE);
	}
}