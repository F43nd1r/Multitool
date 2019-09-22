package com.faendir.lightning_launcher.multitool.calendar

import android.Manifest
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.settings.PreferenceListener
import com.faendir.lightning_launcher.scriptlib.PermissionActivity
import com.google.common.util.concurrent.FutureCallback

/**
 * @author lukas
 * @since 10.08.18
 */
class CalendarFragment : PreferenceFragmentCompat() {
    private lateinit var listener: PreferenceListener

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.calendar, rootKey)
        listener = PreferenceListener(preferenceScreen)
        listener.addPreferenceForSummary(getString(R.string.pref_calendars))
        listener.addPreferenceForSummary(getString(R.string.pref_dateFormat))
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        PermissionActivity.checkForPermission(activity!!, Manifest.permission.READ_CALENDAR).addCallback(object : FutureCallback<Boolean> {
            override fun onSuccess(result: Boolean?) {
                if (result != null && result) {
                    (preferenceScreen.findPreference<CalendarPreference>(getString(R.string.pref_calendars)) as CalendarPreference).refresh()
                }
            }

            override fun onFailure(t: Throwable) {
                t.printStackTrace()
            }
        }) { it.run() }
    }

    override fun onDestroy() {
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        super.onDestroy()
    }
}
