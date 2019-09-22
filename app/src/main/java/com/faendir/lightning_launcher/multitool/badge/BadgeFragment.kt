package com.faendir.lightning_launcher.multitool.badge

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.preference.PreferenceFragmentCompat
import com.faendir.lightning_launcher.multitool.R

/**
 * @author lukas
 * @since 19.07.18
 */
class BadgeFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setHasOptionsMenu(true)
        setPreferencesFromResource(R.xml.badge, rootKey)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.badge, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_help) {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.title_help)
                    .setMessage(R.string.message_helpBadge)
                    .setPositiveButton(R.string.button_ok, null)
                    .show()
        }
        return true
    }
}
