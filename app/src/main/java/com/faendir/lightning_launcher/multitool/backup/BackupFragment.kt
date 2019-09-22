package com.faendir.lightning_launcher.multitool.backup

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.preference.PreferenceFragmentCompat
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.settings.PreferenceListener

/**
 * @author lukas
 * @since 18.07.18
 */
class BackupFragment : PreferenceFragmentCompat() {
    private lateinit var listener: PreferenceListener

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setHasOptionsMenu(true)
        setPreferencesFromResource(R.xml.backup, rootKey)
        listener = PreferenceListener(preferenceScreen)
        val backupChanged = { val activity = activity
            if(activity != null) BackupUtils.scheduleNext(activity) }
        listener.addPreference(getString(R.string.pref_backupTime), backupChanged)
        listener.addPreference(getString(R.string.pref_enableBackup), backupChanged)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.backup, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_help) {
            AlertDialog.Builder(activity).setTitle(R.string.title_help).setMessage(R.string.message_backupHelp).setPositiveButton(R.string.button_ok, null).show()
        }
        return true
    }

    override fun onDestroy() {
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        super.onDestroy()
    }
}
