package com.mgeske.tsgamebuilder;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public SettingsActivity() {
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        initSummary(getPreferenceScreen());
        updatePrefValues(findPreference("game_rules"));
    }

	@Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = findPreference(key);
		updatePrefSummary(preference);

		if(key.equals("game_rules")) {
			updatePrefValues(preference);
		}
	}

	private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            p.setTitle(p.getTitle());
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }
    
    private void updatePrefValues(Preference game_rules_pref) {
		ListPreference listPreference = (ListPreference)game_rules_pref;
		String value = listPreference.getValue();
		if(value.equals(getString(R.string.rule_type_custom))) {
			String[] pref_keys = {"num_monster","num_thunderstone", "num_hero", "num_village", "village_limits", "monster_levels"};
			for(String pref_key : pref_keys) {
				Preference pref = findPreference(pref_key);
				pref.setEnabled(true);
			}
		} else {
			String num_monsters = "3";
			String num_thunderstone = "1";
			String num_hero = "4";
			String num_village = "8";
			boolean village_limits;
			boolean monster_levels;
			if(value.equals(getString(R.string.rule_type_original))) {
				village_limits = false;
				monster_levels = false;
			} else { //thunderstone advance
				village_limits = true;
				monster_levels = true;
			}
			updateStringPref("num_monster", num_monsters);
			updateStringPref("num_thunderstone", num_thunderstone);
			updateStringPref("num_hero", num_hero);
			updateStringPref("num_village", num_village);
			updateBooleanPref("village_limits", village_limits);
			updateBooleanPref("monster_levels", monster_levels);
		}
    }

	private void updateStringPref(String key, String value) {
		EditTextPreference pref = (EditTextPreference)findPreference(key);
		pref.setText(value);
		pref.setEnabled(false);
	}

	private void updateBooleanPref(String key, boolean value) {
		CheckBoxPreference pref = (CheckBoxPreference)findPreference(key);
		pref.setChecked(value);
		pref.setEnabled(false);
	}
}
