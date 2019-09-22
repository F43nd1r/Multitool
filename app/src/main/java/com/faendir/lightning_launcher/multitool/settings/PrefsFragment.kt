package com.faendir.lightning_launcher.multitool.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.faendir.lightning_launcher.multitool.R

/**
 * Created by Lukas on 29.08.2015.
 * preference fragment
 */
class PrefsFragment : PreferenceFragmentCompat() {
    private lateinit var sharedPref: SharedPreferences
    private lateinit var listener: PreferenceListener

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs, rootKey)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        listener = PreferenceListener(preferenceScreen)
        listener.addPreferenceForSummary(getString(R.string.pref_coverMode))
        listener.addPreferenceForSummary(getString(R.string.pref_activePlayers))
        listener.addPreferenceForSummary(getString(R.string.pref_musicDefault))
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onDestroy() {
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        super.onDestroy()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is IdPreference) {
            val f = IdPreference.Dialog.newInstance(preference.getKey())
            f.setTargetFragment(this, 0)
            f.show(fragmentManager!!, "ID_DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}
