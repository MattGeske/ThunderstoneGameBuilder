package com.mgeske.tsgamebuilder.db;

import java.util.ArrayList;
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
import com.mgeske.tsgamebuilder.requirement.CardCostRequirement;
import com.mgeske.tsgamebuilder.requirement.CardTextRequirement;
import com.mgeske.tsgamebuilder.requirement.CardTypeRequirement;
import com.mgeske.tsgamebuilder.requirement.CardValueRequirement;
import com.mgeske.tsgamebuilder.requirement.HasAllClassesRequirement;
import com.mgeske.tsgamebuilder.requirement.HasAnyAttributesRequirement;
import com.mgeske.tsgamebuilder.requirement.HasAnyClassesRequirement;
import com.mgeske.tsgamebuilder.requirement.HasRaceRequirement;
import com.mgeske.tsgamebuilder.requirement.HasStrengthRequirement;
import com.mgeske.tsgamebuilder.requirement.HasWeightRequirement;
import com.mgeske.tsgamebuilder.requirement.LightweightEdgedWeaponRequirement;
import com.mgeske.tsgamebuilder.requirement.MonsterLevelRequirement;
import com.mgeske.tsgamebuilder.requirement.Requirement;
import com.mgeske.tsgamebuilder.requirement.SpecificCardTypeRequirement;

public abstract class RequirementQueryBuilder {
	private static Map<String,String> tableNameMap;
	protected String[] chosenSets;
	protected Requirement requirement;
	
	protected RequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		this.chosenSets = chosenSets;
		this.requirement = requirement;
	}
	
	static {
		tableNameMap = new HashMap<String,String>();
		tableNameMap.put("Monster", "DungeonCard");
		tableNameMap.put("Level1Monster", "DungeonCard");
		tableNameMap.put("Level2Monster", "DungeonCard");
		tableNameMap.put("Level3Monster", "DungeonCard");
		tableNameMap.put("Treasure", "DungeonCard");
		tableNameMap.put("Trap", "DungeonCard");
		tableNameMap.put("Guardian", "DungeonBossCard");
		tableNameMap.put("Thunderstone", "DungeonBossCard");
		tableNameMap.put("Hero", "HeroCard");
		tableNameMap.put("Village", "VillageCard");
	}
	
	public Iterator<? extends Card> queryMatchingCards(CardList currentCards, SQLiteDatabase db,
			Map<String, Requirement> allRequirements, boolean includeAllSets) {
		String[] cardIds = getMatchingCardIds(currentCards, db, includeAllSets);
		
		String mainTableName = getMainTableName();
		CardBuilder cardBuilder = CardBuilder.getCardBuilder(mainTableName, db, allRequirements, chosenSets);
		
		return new CardResultIterator(cardIds, cardBuilder);
	}
	
	protected String getMainTableName() {
		String cardType = requirement.getRequiredOn();
		return tableNameMap.get(cardType);
	}
	
	protected String[] getMatchingCardIds(CardList currentCards, SQLiteDatabase db, boolean includeAllSets) {
		String tableName = getTableName()+", Card_ThunderstoneSet, ThunderstoneSet";
		String cardIdColumn = getCardIdColumn();
		String[] columns = new String[]{"distinct("+cardIdColumn+") as cardId"};
		String selection = getSelection();
		String[] selectionArgs = getSelectionArgs();

		List<? extends Card> currentCardsOfSameType;
		if(currentCards != null) {
			currentCardsOfSameType = currentCards.getCardsByType(requirement.getRequiredOn());
		} else {
			currentCardsOfSameType = new ArrayList<Card>();
		}
		
		String additionalWhere = buildWhereNotInCurrentCards(cardIdColumn, currentCardsOfSameType);
		if(!includeAllSets) {
			additionalWhere += " and Card_ThunderstoneSet.cardId="+cardIdColumn+" and Card_ThunderstoneSet.cardTableName='"+getMainTableName() +
							   "' and Card_ThunderstoneSet.setId=ThunderstoneSet._ID and "+buildInClause("ThunderstoneSet.setName", chosenSets, false);
		}
		
		String[] matchingCardIds = null;
		Cursor c = null;
		try {
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(tableName);
			queryBuilder.appendWhere(additionalWhere);
			c = queryBuilder.query(db, columns, selection, selectionArgs, null, null, null, null);
			matchingCardIds = new String[c.getCount()];
			while(c.moveToNext()) {
				String cardId = c.getString(c.getColumnIndexOrThrow("cardId"));
				matchingCardIds[c.getPosition()] = cardId;
			}
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return matchingCardIds;
	}
	
	protected abstract String getTableName();
	protected abstract String getCardIdColumn();
	protected abstract String getSelection();
	protected abstract String[] getSelectionArgs();
	
	private String buildWhereNotInCurrentCards(String cardIdColumn, List<? extends Card> currentCards) {
		String[] values = new String[currentCards.size()];
		for(int i = 0; i < currentCards.size(); i++) {
			Card card = currentCards.get(i);
			values[i] = card.getCardId();
		}
		return buildInClause(cardIdColumn, values, true);
	}
	
	protected String buildInClause(String columnName, String[] values, boolean invert) {
		StringBuilder sb = new StringBuilder();
		sb.append(columnName);
		if(invert) {
			sb.append(" not");
		}
		sb.append(" in (");
		for(int i = 0; i < values.length; i++) {
			if(i > 0) {
				sb.append(",");
			}
			sb.append("'");
			sb.append(values[i]);
			sb.append("'");
		}
		sb.append(")");
		return sb.toString();
	}
	
	public static RequirementQueryBuilder getRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		if(requirement instanceof HasAnyAttributesRequirement) {
			return new HasAnyAttributesRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof HasAnyClassesRequirement) {
			return new HasAnyClassesRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof HasAllClassesRequirement) {
			return new HasAllClassesRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof HasStrengthRequirement) {
			return new HasStrengthRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof HasWeightRequirement) {
			return new HasWeightRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof HasRaceRequirement) {
			return new HasRaceRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof LightweightEdgedWeaponRequirement) {
			return new LightweightEdgedWeaponRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof CardTypeRequirement) {
			return new CardTypeRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof SpecificCardTypeRequirement) {
			return new SpecificCardTypeRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof CardTextRequirement) {
			return new CardTextRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof MonsterLevelRequirement) {
			return new MonsterLevelRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof CardCostRequirement) {
			return new CardCostRequirementQueryBuilder(chosenSets, requirement);
		} else if(requirement instanceof CardValueRequirement) {
			return new CardValueRequirementQueryBuilder(chosenSets, requirement);
		} else {
			throw new RuntimeException("Unknown requirement type "+requirement.getClass());
		}
	}
}

class HasAnyAttributesRequirementQueryBuilder extends RequirementQueryBuilder {
	protected HasAnyAttributesRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getTableName() {
		return "Card_CardAttribute, CardAttribute";
	}
	
	@Override
	protected String getCardIdColumn() {
		return "Card_CardAttribute.cardId";
	}

	@Override
	protected String getSelection() {
		int num_attributes = requirement.getValues().size();
		return CardDatabase.buildInClausePlaceholders("CardAttribute.attributeName", num_attributes, false)+ 
				" and Card_CardAttribute.cardTableName = ? and Card_CardAttribute.attributeId = CardAttribute._ID";
	}

	@Override
	protected String[] getSelectionArgs() {
		String mainTableName = getMainTableName();
		int num_attributes = requirement.getValues().size();
		String[] selectionArgs = new String[num_attributes+1];
		requirement.getValues().toArray(selectionArgs);
		selectionArgs[num_attributes] = mainTableName;
		return selectionArgs;
	}
}

class HasAnyClassesRequirementQueryBuilder extends RequirementQueryBuilder {
	protected HasAnyClassesRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getTableName() {
		return "Card_CardClass, CardClass";
	}
	
	@Override
	protected String getCardIdColumn() {
		return "Card_CardClass.cardId";
	}

	@Override
	protected String getSelection() {
		int num_classes = requirement.getValues().size();
		return CardDatabase.buildInClausePlaceholders("CardClass.className", num_classes, false)+ 
				" and Card_CardClass.cardTableName = ? and Card_CardClass.classId = CardClass._ID";
	}

	@Override
	protected String[] getSelectionArgs() {
		String mainTableName = getMainTableName();
		int num_classes = requirement.getValues().size();
		String[] selectionArgs = new String[num_classes+1];
		requirement.getValues().toArray(selectionArgs);
		selectionArgs[num_classes] = mainTableName;
		return selectionArgs;
	}
}

class HasAllClassesRequirementQueryBuilder extends RequirementQueryBuilder {
	protected HasAllClassesRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	private final String SUBQUERY = "Card_CardClass.cardId in (SELECT Card_CardClass.cardId from Card_CardClass, CardClass where Card_CardClass.classId=CardClass._ID and CardClass.className=?)";
	
	@Override
	protected String getTableName() {
		return "Card_CardClass";
	}
	
	@Override
	protected String getCardIdColumn() {
		return "Card_CardClass.cardId";
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
	protected String getSelection() {
		int num_classes = requirement.getValues().size();
		
		String selection = buildSubQueryPlaceholders(num_classes)+" and Card_CardClass.cardTableName = ?";
		return selection;
	}

	@Override
	protected String[] getSelectionArgs() {
		String mainTableName = getMainTableName();
		int num_classes = requirement.getValues().size();
		String[] selectionArgs = new String[num_classes+1];
		requirement.getValues().toArray(selectionArgs);
		selectionArgs[num_classes] = mainTableName;
		return selectionArgs;
	}
}

class HasStrengthRequirementQueryBuilder extends RequirementQueryBuilder {
	protected HasStrengthRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getCardIdColumn() {
		return "HeroCard._ID";
	}
	
	@Override
	protected String getTableName() {
		return "HeroCard";
	}

	@Override
	protected String getSelection() {
		return "strength >= ?";
	}

	@Override
	protected String[] getSelectionArgs() {
		HasStrengthRequirement strengthRequirement = (HasStrengthRequirement)requirement;
		int strength = strengthRequirement.getStrength();
		return new String[]{Integer.toString(strength)};
	}
}

class HasWeightRequirementQueryBuilder extends RequirementQueryBuilder {
	protected HasWeightRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getCardIdColumn() {
		return "VillageCard._ID";
	}
	
	@Override
	protected String getTableName() {
		return "VillageCard";
	}

	@Override
	protected String getSelection() {
		return "weight is not null and weight = ?";
	}

	@Override
	protected String[] getSelectionArgs() {
		HasWeightRequirement weightRequirement = (HasWeightRequirement)requirement;
		int weight = weightRequirement.getWeight();
		return new String[]{Integer.toString(weight)};
	}
}

class HasRaceRequirementQueryBuilder extends RequirementQueryBuilder {
	protected HasRaceRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getCardIdColumn() {
		return "HeroCard._ID";
	}
	
	@Override
	protected String getTableName() {
		return "HeroCard";
	}

	@Override
	protected String getSelection() {
		return "race = ?";
	}

	@Override
	protected String[] getSelectionArgs() {
		HasRaceRequirement raceRequirement = (HasRaceRequirement)requirement;
		return new String[]{raceRequirement.getRace()};
	}
}

class LightweightEdgedWeaponRequirementQueryBuilder extends HasAllClassesRequirementQueryBuilder {
	protected LightweightEdgedWeaponRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getTableName() {
		return "VillageCard, Card_CardClass";
	}
	
	@Override
	protected String getCardIdColumn() {
		return "VillageCard._ID";
	}

	@Override
	protected String getSelection() {
		return "VillageCard._ID=Card_CardClass.cardId and Card_CardClass.cardTableName='VillageCard' and " +
				"VillageCard.weight <= 3 and "+buildSubQueryPlaceholders(2);
	}

	@Override
	protected String[] getSelectionArgs() {
		return new String[]{"Weapon","Edged"};
	}
}

class CardTypeRequirementQueryBuilder extends RequirementQueryBuilder {
	protected CardTypeRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getCardIdColumn() {
		return getTableName()+"._ID";
	}
	
	@Override
	protected String getTableName() {
		return getMainTableName();
	}

	@Override
	protected String getSelection() {
		return null;
	}

	@Override
	protected String[] getSelectionArgs() {
		return null;
	}
}

class SpecificCardTypeRequirementQueryBuilder extends CardTypeRequirementQueryBuilder {
	private String[] cardTypes;
	
	protected SpecificCardTypeRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
		String cardType = requirement.getRequiredOn();
		if("Thunderstone".equals(cardType)) {
			//special case: "Thunderstone" and "ThunderstoneBearer" are the same
			cardTypes = new String[]{cardType, "ThunderstoneBearer"};
		} else {
			cardTypes = new String[]{cardType};
		}
	}

	@Override
	protected String getSelection() {
		if(getMainTableName() == "DungeonCard" || getMainTableName() == "DungeonBossCard") {
			String selection = CardDatabase.buildInClausePlaceholders("cardType", cardTypes.length, false);
			return selection;
		}
		return null;
	}

	@Override
	protected String[] getSelectionArgs() {
		if(getMainTableName() == "DungeonCard" || getMainTableName() == "DungeonBossCard") {
			return cardTypes;
		}
		return null;
	}
}

class CardTextRequirementQueryBuilder extends RequirementQueryBuilder {

	protected CardTextRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getCardIdColumn() {
		return getTableName()+"._ID";
	}
	
	@Override
	protected String getTableName() {
		return getMainTableName();
	}

	@Override
	protected String getSelection() {
		return "cardName like ? or description like ?";
	}

	@Override
	protected String[] getSelectionArgs() {
		CardTextRequirement cardTextRequirement = (CardTextRequirement)requirement;
		String searchText = "%"+cardTextRequirement.getSearchText()+"%";
		return new String[]{searchText, searchText};
	}
}

class MonsterLevelRequirementQueryBuilder extends RequirementQueryBuilder {

	protected MonsterLevelRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getTableName() {
		return "DungeonCard";
	}

	@Override
	protected String getCardIdColumn() {
		return "DungeonCard._ID";
	}

	@Override
	protected String getSelection() {
		return "level = ?";
	}

	@Override
	protected String[] getSelectionArgs() {
		MonsterLevelRequirement monsterLevelRequirement = (MonsterLevelRequirement)requirement;
		return new String[]{Integer.toString(monsterLevelRequirement.getMonsterLevel())};
	}
	
}

class CardCostRequirementQueryBuilder extends RequirementQueryBuilder {
	protected CardCostRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getTableName() {
		return "VillageCard";
	}

	@Override
	protected String getCardIdColumn() {
		return "VillageCard._ID";
	}

	@Override
	protected String getSelection() {
		return "goldCost = ?";
	}

	@Override
	protected String[] getSelectionArgs() {
		CardCostRequirement cardCostRequirement = (CardCostRequirement)requirement;
		return new String[]{Integer.toString(cardCostRequirement.getCost())};
	}
}

class CardValueRequirementQueryBuilder extends RequirementQueryBuilder {
	protected CardValueRequirementQueryBuilder(String[] chosenSets, Requirement requirement) {
		super(chosenSets, requirement);
	}

	@Override
	protected String getTableName() {
		return "VillageCard";
	}

	@Override
	protected String getCardIdColumn() {
		return "VillageCard._ID";
	}

	@Override
	protected String getSelection() {
		return "goldValue = ?";
	}

	@Override
	protected String[] getSelectionArgs() {
		CardValueRequirement cardValueRequirement = (CardValueRequirement)requirement;
		return new String[]{Integer.toString(cardValueRequirement.getValue())};
	}
}

class CardResultIterator implements Iterator<Card> {
	private int nextItemPosition = 0;
	private String[] cardIds;
	private CardBuilder cardBuilder;
	private Random random = new Random();
	
	public CardResultIterator(String[] cardIds, CardBuilder cardBuilder) {
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