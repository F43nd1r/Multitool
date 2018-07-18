package com.faendir.lightning_launcher.multitool.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.faendir.lightning_launcher.multitool.R;

/**
 * Created by Lukas on 29.08.2015.
 * preference fragment
 */
public class PrefsFragment extends PreferenceFragment {
    private SharedPreferences sharedPref;
    private PreferenceListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        listener = new PreferenceListener(getPreferenceScreen());
        listener.addPreferenceForSummary(getString(R.string.pref_coverMode));
        listener.addPreferenceForSummary(getString(R.string.pref_activePlayers));
        listener.addPreferenceForSummary(getString(R.string.pref_musicDefault));
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onDestroy() {
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }
}
