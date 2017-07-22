package com.mgeske.tsgamebuilder.dialogs;

import com.mgeske.tsgamebuilder.card.ThunderstoneCard;

public class ThunderstoneCardInformationDialog extends CardInformationDialog<ThunderstoneCard> {

    @Override
    protected String getSubheaderText() {
        return getCard().getThunderstoneType();
    }
}
