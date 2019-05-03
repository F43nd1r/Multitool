package com.faendir.lightning_launcher.multitool.settings

import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.AttributeSet
import androidx.preference.MultiSelectListPreference

/**
 * @author F43nd1r
 * @since 08.11.2016
 */

class PlayersPreference(context: Context, attrs: AttributeSet) : MultiSelectListPreference(context, attrs), SummaryPreference, HasPlayerEntries {

    private val selectedEntries: List<CharSequence>
        get() {
            val entries = entries.toList()
            val values = entryValues.toList()
            return getValues().asSequence().map { values.indexOf(it) }.filter { index -> index >= 0 }
                    .map { entries[it].toString()}.sorted().toList()
        }

    override val summaryText: CharSequence
        get() = TextUtils.join(", ", selectedEntries)

    init {
        discoverPlayers(context.packageManager)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return if (a.getBoolean(index, false)) {
            context.packageManager.queryBroadcastReceivers(Intent(Intent.ACTION_MEDIA_BUTTON), 0)
                    .map { info -> info.activityInfo.packageName }.toSet()
        } else {
            emptySet<Any>()
        }
    }
}
