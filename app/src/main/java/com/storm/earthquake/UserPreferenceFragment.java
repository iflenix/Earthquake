package com.storm.earthquake;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by StorM on 22.04.2015.
 */
public class UserPreferenceFragment extends PreferenceFragment {
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
    public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userpreferences);
        addPreferencesFromResource(R.xml.test_preferences);
    }

}
