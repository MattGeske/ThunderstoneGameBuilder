package com.mgeske.tsgamebuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

@SuppressWarnings("deprecation")
public class ChooseSetsActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private Set<String> chosenSets = new HashSet<String>();
	private List<String> allSets;

	public ChooseSetsActivity() {
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		CardDatabase cardDb = null;
		try {
			cardDb = new CardDatabase(this);
			allSets = cardDb.getThunderstoneSets();
		} finally {
			if(cardDb != null) {
				cardDb.close();
			}
		}
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String currentSetsRaw = preferences.getString("chosenSets", "");
		for(String chosenSet : currentSetsRaw.split(",")) {
			if(!"".equals(chosenSet)) {
				chosenSets.add(chosenSet);
			}
		}
        this.setPreferenceScreen(createPreferenceHierarchy());
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean setIsChosen = (Boolean)newValue;
		String setName = preference.getTitle().toString();
		logger.info("Preference changed: "+setName+", new value="+setIsChosen);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = preferences.edit();
		if(setIsChosen && !chosenSets.contains(setName)){
			chosenSets.add(setName);
		} else {
			chosenSets.remove(setName);
		}
		editor.putString("chosenSets", TextUtils.join(",", chosenSets));
		editor.commit();
		return true;
	}
	
	public PreferenceScreen createPreferenceHierarchy(){
	    PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

	    //create a root category
	    PreferenceCategory rootCategory = new PreferenceCategory(this);
	    rootCategory.setTitle("Thunderstone Sets");
	    root.addPreference(rootCategory);
	    
		for(String setName : allSets) {
			CheckBoxPreference thunderstoneSetPreference = new CheckBoxPreference(this);
			thunderstoneSetPreference.setTitle(setName);
			thunderstoneSetPreference.setOnPreferenceChangeListener(this);
			thunderstoneSetPreference.setChecked(chosenSets.contains(setName));
			rootCategory.addPreference(thunderstoneSetPreference);
		}

	    return root;
	}
}
