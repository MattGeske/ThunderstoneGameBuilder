package com.mgeske.tsgamebuilder.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mgeske.tsgamebuilder.R;
import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.DungeonCard;
import com.mgeske.tsgamebuilder.card.GuardianCard;
import com.mgeske.tsgamebuilder.card.HeroCard;
import com.mgeske.tsgamebuilder.card.ThunderstoneCard;
import com.mgeske.tsgamebuilder.card.VillageCard;

public abstract class CardInformationDialog<T extends Card> extends DialogFragment {
    private LayoutInflater inflater;

    public static CardInformationDialog getCardInformationDialog(Card card) {
        Bundle arguments = new Bundle();
        arguments.putParcelable("card", card);
        CardInformationDialog dialog;
        if(card instanceof DungeonCard) {
             dialog = new DungeonCardInformationDialog();
        } else if(card instanceof GuardianCard) {
            dialog = new GuardianCardInformationDialog();
        } else if(card instanceof ThunderstoneCard) {
            dialog = new ThunderstoneCardInformationDialog();
        } else if(card instanceof HeroCard) {
            dialog = new HeroCardInformationDialog();
        } else if(card instanceof VillageCard) {
            dialog = new VillageCardInformationDialog();
        } else {
            throw new RuntimeException("Couldn't find appropriate CardInformationDialog subclass for card "+card);
        }
        dialog.setArguments(arguments);
        return dialog;
    }

    public CardInformationDialog() {}

    protected T getCard() {
        Bundle arguments = getArguments();
        return (T)arguments.getParcelable("card");
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

    private View getCardView() {
        Card card = getCard();
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
        TextView tv = (TextView)cardView.findViewById(textViewId);
        tv.setText(text);
        tv.setVisibility(View.VISIBLE);
    }
}
