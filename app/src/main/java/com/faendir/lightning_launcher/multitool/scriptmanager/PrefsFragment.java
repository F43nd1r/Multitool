package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.faendir.lightning_launcher.multitool.R;

/**
 * Created by Lukas on 29.08.2015.
 * preference fragment
 */
public class PrefsFragment extends PreferenceFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_scriptmanager);
    }
}
