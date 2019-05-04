package com.faendir.lightning_launcher.multitool.badge

import android.app.AlertDialog
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.util.BaseActivity
import com.faendir.lightning_launcher.multitool.util.IntentChooserFragment
import com.faendir.lightning_launcher.multitool.util.IntentInfo
import com.faendir.lightning_launcher.multitool.util.notification.NotificationDistributorService
import java9.util.Comparators

/**
 * @author F43nd1r
 * @since 07.11.2017
 */

class AppChooser : BaseActivity(R.layout.content_app_chooser) {
    private var byRelevance: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_appChooser)
        byRelevance = true
        sort()
        if (NotificationDistributorService.isDisabled(this)) {
            NotificationDistributorService.askForEnable(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_badge_app_chooser, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun sort() {
        val fragment = (supportFragmentManager.findFragmentById(R.id.chooserFragment) as IntentChooserFragment?)!!
        if (byRelevance) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            val highPriority: MutableSet<String> = sharedPref.getStringSet(getString(R.string.key_badgeIntentPackages), emptySet())!!
            val prefix = getString(R.string.unread_prefix)
            val midPriority = sharedPref.all.keys.filter { it.startsWith(prefix) }.map { it.substring(prefix.length) }.toSet()
            fragment.setComparator (Comparator { o1, o2 ->

                val pn1 = o1.intent.component?.packageName

                val pn2 = o2.intent.component?.packageName
                val p1 = if (highPriority.contains(pn1)) 2 else if (midPriority.contains(pn1)) 1 else 0
                val p2 = if (highPriority.contains(pn2)) 2 else if (midPriority.contains(pn2)) 1 else 0
                val compare = Integer.compare(p2, p1)
                if (compare != 0) compare else o1.compareTo(o2)
            })
        } else {
            fragment.setComparator(Comparators.comparing<IntentInfo, String> { it.name })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_change_sorting) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.action_change_sorting)
                    .setSingleChoiceItems(R.array.sortings, if (byRelevance) 0 else 1) { dialog, which ->
                        byRelevance = which == 0
                        sort()
                        dialog.dismiss()
                    }
                    .show()
        }
        return super.onOptionsItemSelected(item)
    }
}
