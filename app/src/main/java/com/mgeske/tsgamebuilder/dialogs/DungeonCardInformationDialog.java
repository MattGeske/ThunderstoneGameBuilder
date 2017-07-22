package com.mgeske.tsgamebuilder.dialogs;

import android.view.View;

import com.mgeske.tsgamebuilder.R;
import com.mgeske.tsgamebuilder.card.DungeonCard;

public class DungeonCardInformationDialog extends CardInformationDialog<DungeonCard> {
//    private DungeonCard card;

//    public DungeonCardInformationDialog(DungeonCard card) {
//        super(card);
//        this.card = card;
//    }

    @Override
    protected String getSubheaderText() {
        DungeonCard card = getCard();
        String subheaderText = card.getCardType();
        if(subheaderText.startsWith("Monster")) {
            subheaderText += " - Level "+card.getLevel();
        }
        return subheaderText;
    }

    @Override
    protected void addCardTypeSpecificAttributes(View cardView) {
        DungeonCard card = getCard();

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