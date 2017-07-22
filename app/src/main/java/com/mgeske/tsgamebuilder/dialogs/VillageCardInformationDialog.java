package com.mgeske.tsgamebuilder.dialogs;

import android.text.TextUtils;
import android.view.View;

import com.mgeske.tsgamebuilder.R;
import com.mgeske.tsgamebuilder.card.VillageCard;

public class VillageCardInformationDialog extends CardInformationDialog<VillageCard> {

    @Override
    protected String getSubheaderText() {
        return TextUtils.join(" - ", getCard().getClasses());
    }

    @Override
    protected void addCardTypeSpecificAttributes(View cardView) {
        VillageCard card = getCard();
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
