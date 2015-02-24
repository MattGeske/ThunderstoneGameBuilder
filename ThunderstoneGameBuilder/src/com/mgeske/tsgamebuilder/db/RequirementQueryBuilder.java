package com.mgeske.tsgamebuilder.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.mgeske.tsgamebuilder.db.CardBuilder;
import com.mgeske.tsgamebuilder.card.Card;
import com.mgeske.tsgamebuilder.card.CardList;
import com.mgeske.tsgamebuilder.requirement.CardTypeRequirement;
import com.mgeske.tsgamebuilder.requirement.HasAllClassesRequirement;
import com.mgeske.tsgamebuilder.requirement.HasAnyAttributesRequirement;
import com.mgeske.tsgamebuilder.requirement.HasAnyClassesRequirement;
import com.mgeske.tsgamebuilder.requirement.HasStrengthRequirement;
import com.mgeske.tsgamebuilder.requirement.LightweightEdgedWeaponRequirement;
import com.mgeske.tsgamebuilder.requirement.Requirement;

public abstract class RequirementQueryBuilder {
	private static Map<String,String> tableNameMap;
	private static Map<Requirement,RequirementQueryBuilder> instanceMap = new HashMap<Requirement,RequirementQueryBuilder>();
	
	static {
		tableNameMap = new HashMap<String,String>();
		tableNameMap.put("Monster", "DungeonCard");
		tableNameMap.put("Level1Monster", "DungeonCard");
		tableNameMap.put("Level2Monster", "DungeonCard");
		tableNameMap.put("Level3Monster", "DungeonCard");
		tableNameMap.put("Thunderstone", "DungeonBossCard");
		tableNameMap.put("Hero", "HeroCard");
		tableNameMap.put("Village", "VillageCard");
	}
	
	public Iterator<? extends Card> queryMatchingCards(Requirement requirement, CardList currentCards, SQLiteDatabase db, Map<String, Requirement> allRequirements) {
		String[] cardIds = getMatchingCardIds(requirement, currentCards, db);
		
		String mainTableName = getMainTableName(requirement);
		CardBuilder<? extends Card> cardBuilder = CardBuilder.getCardBuilder(mainTableName, db, allRequirements);
		
		return new CardResultIterator(cardIds, cardBuilder);
	}
	
	protected String getMainTableName(Requirement requirement) {
		String cardType = requirement.getRequiredOn();
		return tableNameMap.get(cardType);
	}
	
	protected String[] getMatchingCardIds(Requirement requirement, CardList currentCards, SQLiteDatabase db) {
		String tableName = getTableName(requirement);
		String cardIdColumn = getCardIdColumn();
		String[] columns = new String[]{"distinct("+cardIdColumn+")"};
		String selection = getSelection(requirement, currentCards);
		String[] selectionArgs = getSelectionArgs(requirement, currentCards);
		
		List<? extends Card> currentCardsOfSameType = currentCards.getCardsByType(requirement.getRequiredOn());
		String additionalWhere = buildAdditionalWhere(cardIdColumn, currentCardsOfSameType);
		
		String[] matchingCardIds = null;
		Cursor c = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tableName);
			queryBuilder.appendWhere(additionalWhere);
			c = queryBuilder.query(db, columns, selection, selectionArgs, null, null, null, null);
			matchingCardIds = new String[c.getCount()];
			while(c.moveToNext()) {
				String cardId = c.getString(c.getColumnIndexOrThrow(cardIdColumn));
				matchingCardIds[c.getPosition()] = cardId;
			}
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return matchingCardIds;
	}
	
	protected abstract String getTableName(Requirement requirement);
	protected abstract String getSelection(Requirement requirement, CardList currentCards);
	protected abstract String[] getSelectionArgs(Requirement requirement, CardList currentCards);
	protected String getCardIdColumn() {
		return "cardId";
	}
	
	protected String buildInClausePlaceholders(int num_values) {
		StringBuilder sb = new StringBuilder(2*num_values+1);
		sb.append("(");
		for(int i = 0; i < num_values; i++) {
			if(i > 0) {
				sb.append(",");
			}
			sb.append("?");
		}
		sb.append(")");
		return sb.toString();
	}
	
	protected String buildAdditionalWhere(String cardIdColumn, List<? extends Card> currentCards) {
		StringBuilder sb = new StringBuilder();
		sb.append(cardIdColumn);
		sb.append(" not in (");
		for(int i = 0; i < currentCards.size(); i++) {
			if(i > 0) {
				sb.append(",");
			}
			Card card = currentCards.get(i);
			sb.append(card.getCardId());
		}
		sb.append(")");
		return sb.toString();
	}
	
	public static RequirementQueryBuilder getRequirementQueryBuilder(Requirement requirement) {
		if(instanceMap.containsKey(requirement)) {
			return instanceMap.get(requirement);
		}
		RequirementQueryBuilder instance;
		if(requirement instanceof HasAnyAttributesRequirement) {
			instance = new HasAnyAttributesRequirementQueryBuilder();
		} else if(requirement instanceof HasAnyClassesRequirement) {
			instance = new HasAnyClassesRequirementQueryBuilder();
		} else if(requirement instanceof HasAllClassesRequirement) {
			instance = new HasAllClassesRequirementQueryBuilder();
		} else if(requirement instanceof HasStrengthRequirement) {
			instance = new HasStrengthRequirementQueryBuilder();
		} else if(requirement instanceof LightweightEdgedWeaponRequirement) {
			instance = new LightweightEdgedWeaponRequirementQueryBuilder();
		} else if(requirement instanceof CardTypeRequirement) {
			instance = new CardTypeRequirementQueryBuilder();
		} else {
			throw new RuntimeException("Unknown requirement type "+requirement.getClass());
		}
		instanceMap.put(requirement, instance);
		return instance;
	}
}

class HasAnyAttributesRequirementQueryBuilder extends RequirementQueryBuilder {
	@Override
	protected String getTableName(Requirement requirement) {
		return "Card_CardAttribute, CardAttribute";
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		int num_attributes = requirement.getValues().size();
		return "CardAttribute.attributeName in "+buildInClausePlaceholders(num_attributes)+ 
				" and Card_CardAttribute.cardTableName = ? and Card_CardAttribute.attributeId = CardAttribute._ID";
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		String mainTableName = getMainTableName(requirement);
		int num_attributes = requirement.getValues().size();
		String[] selectionArgs = new String[num_attributes+1];
		requirement.getValues().toArray(selectionArgs);
		selectionArgs[num_attributes] = mainTableName;
		return selectionArgs;
	}
}

class HasAnyClassesRequirementQueryBuilder extends RequirementQueryBuilder {
	@Override
	protected String getTableName(Requirement requirement) {
		return "Card_CardClass, CardClass";
	}

	@Override
	protected String getSelection(Requirement requirement,
			CardList currentCards) {
		int num_classes = requirement.getValues().size();
		return "CardClass.className in "+buildInClausePlaceholders(num_classes)+ 
				" and Card_CardClass.cardTableName = ? and Card_CardClass.classId = CardClass._ID";
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		String mainTableName = getMainTableName(requirement);
		int num_classes = requirement.getValues().size();
		String[] selectionArgs = new String[num_classes+1];
		requirement.getValues().toArray(selectionArgs);
		selectionArgs[num_classes] = mainTableName;
		return selectionArgs;
	}
}

class HasAllClassesRequirementQueryBuilder extends RequirementQueryBuilder {
	private final String SUBQUERY = "cardId in (SELECT cardId from Card_CardClass, CardClass where Card_CardClass.classId=CardClass._ID and CardClass.className=?)";
	@Override
	protected String getTableName(Requirement requirement) {
		return "Card_CardClass";
	}
	
	protected String buildSubQueryPlaceholders(int num_classes) {
		//length: there will be num_classes subqueries, and there will be num_classes-1 " and " strings 
		StringBuilder sb = new StringBuilder(SUBQUERY.length()*num_classes+5*(num_classes-1));
		for(int i = 0; i < num_classes; i++) {
			if(i > 0) {
				sb.append(" and ");
			}
			sb.append(SUBQUERY);
		}
		return sb.toString();
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		int num_classes = requirement.getValues().size();
		
		String selection = buildSubQueryPlaceholders(num_classes)+" and cardTableName = ?";
		return selection;
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		String mainTableName = getMainTableName(requirement);
		int num_classes = requirement.getValues().size();
		String[] selectionArgs = new String[num_classes+1];
		requirement.getValues().toArray(selectionArgs);
		selectionArgs[num_classes] = mainTableName;
		return selectionArgs;
	}
}

class HasStrengthRequirementQueryBuilder extends RequirementQueryBuilder {
	@Override
	protected String getCardIdColumn() {
		return "_ID";
	}
	
	@Override
	protected String getTableName(Requirement requirement) {
		return "HeroCard";
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		return "strength=?";
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		HasStrengthRequirement strengthRequirement = (HasStrengthRequirement)requirement;
		int strength = strengthRequirement.getStrength();
		return new String[]{Integer.toString(strength)};
	}
}

class LightweightEdgedWeaponRequirementQueryBuilder extends HasAllClassesRequirementQueryBuilder {
	@Override
	protected String getTableName(Requirement requirement) {
		return "VillageCard, Card_CardClass";
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		return "VillageCard._ID=Card_CardClass.cardId and Card_CardClass.cardTableName='VillageCard' and " +
				"VillageCard.weight <= 3 and "+buildSubQueryPlaceholders(2);
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		return new String[]{"Weapon","Edged"};
	}
}

class CardTypeRequirementQueryBuilder extends RequirementQueryBuilder {
	@Override
	protected String getCardIdColumn() {
		return "_ID";
	}
	
	@Override
	protected String getTableName(Requirement requirement) {
		return getMainTableName(requirement);
	}

	@Override
	protected String getSelection(Requirement requirement, CardList currentCards) {
		return null;
	}

	@Override
	protected String[] getSelectionArgs(Requirement requirement, CardList currentCards) {
		return null;
	}
}

class CardResultIterator implements Iterator<Card> {
	private int nextItemPosition = 0;
	private String[] cardIds;
	private CardBuilder<? extends Card> cardBuilder;
	private Random random = new Random();
	
	public CardResultIterator(String[] cardIds, CardBuilder<? extends Card> cardBuilder) {
		this.cardIds = cardIds;
		this.cardBuilder = cardBuilder;
		
		//randomize the order of the cardIds
		for(int i = cardIds.length-1; i > 0; i--) {
			int j = random.nextInt(i+1);
			String temp = cardIds[i];
			cardIds[i] = cardIds[j];
			cardIds[j] = temp;
		}
	}

	@Override
	public boolean hasNext() {
		return nextItemPosition < cardIds.length;
	}

	@Override
	public Card next() {
		if(!hasNext()) {
			throw new NoSuchElementException("No remaining card ids");
		}
		String nextCardId = cardIds[nextItemPosition];
		nextItemPosition++;
		return cardBuilder.buildCard(nextCardId);
	}

	@Override
	public void remove() {
		//do nothing
	}
	
}