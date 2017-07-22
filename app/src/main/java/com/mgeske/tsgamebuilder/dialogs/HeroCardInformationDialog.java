package com.mgeske.tsgamebuilder.dialogs;

import android.text.TextUtils;
import android.view.View;

import com.mgeske.tsgamebuilder.R;
import com.mgeske.tsgamebuilder.card.HeroCard;

public class HeroCardInformationDialog extends CardInformationDialog<HeroCard> {

    @Override
    protected String getSubheaderText() {
        HeroCard card = getCard();
        return TextUtils.join(" - ", card.getClasses())+"\n"+card.getRace();
    }

    @Override
    protected void addCardTypeSpecificAttributes(View cardView) {
        setText(cardView, R.id.strength, "Strength: "+getCard().getStrength());
        cardView.findViewById(R.id.separator_after_attributes).setVisibility(View.VISIBLE);
    }
}
