package com.faendir.lightning_launcher.multitool.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.faendir.lightning_launcher.multitool.R;

/**
 * Created by Lukas on 29.08.2015.
 * preference fragment
 */
public class PrefsFragment extends PreferenceFragmentCompat {
    private SharedPreferences sharedPref;
    private PreferenceListener listener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.prefs, rootKey);
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

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof IdPreference) {
            DialogFragment f = IdPreference.Dialog.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), "ID_DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
