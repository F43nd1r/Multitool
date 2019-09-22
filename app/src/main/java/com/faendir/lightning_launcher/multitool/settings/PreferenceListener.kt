package com.faendir.lightning_launcher.multitool.settings

import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import java.util.*

/**
 * Created by Lukas on 14.12.2015.
 * Manages Preference change events
 */
class PreferenceListener(private val screen: PreferenceScreen) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val map: MutableMap<String, Wrapper> = HashMap()

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (map.keys.contains(key)) {
            if (map[key]!!.setSummaryToValue) {
                setSummary(key)
            }
            map[key]!!.action?.invoke()
        }
    }

    private fun setSummary(key: String) {
        val preference: Preference? = screen.findPreference(key)
        preference?.summary = when (preference) {
            is SummaryPreference -> (preference as SummaryPreference).summaryText
            is ListPreference -> preference.entry
            null -> ""
            else -> preference.sharedPreferences.all[key].toString()
        }
    }

    /**
     * add an action to execute when the preference changes
     * @param key      the preference identifier
     * @param action   the action
     */
    fun addPreference(key: String, action: (()->Unit)?) = addPreference(key, false, action)

    /**
     * add an action to execute when the preference changes and keep its summary set to its value
     * @param key      the preference identifier
     * @param action   the action
     */
    fun addPreferenceForSummary(key: String, action: (()->Unit)? = null) = addPreference(key, true, action)

    /**
     * add an action to execute when the preference changes and optionally keep its summary set to its value
     * @param key               the preference identifier
     * @param setSummaryToValue if the summary should be kept set to the value
     * @param action            the action
     */
    private fun addPreference(key: String, setSummaryToValue: Boolean, action: (()->Unit)? = null) {
        map[key] = Wrapper(action, setSummaryToValue)
        if (setSummaryToValue) {
            setSummary(key)
        }
    }

    private data class Wrapper internal constructor(internal val action: (()->Unit)?, internal val setSummaryToValue: Boolean)
}
