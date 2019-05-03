package com.faendir.lightning_launcher.multitool.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

/**
 * @author F43nd1r
 * @since 08.11.2016
 */

class DefaultPlayerPreference(context: Context, attrs: AttributeSet) : ListPreference(context, attrs), SummaryPreference, HasPlayerEntries {

    override val summaryText: CharSequence
        get() = entry

    init {
        discoverPlayers(context.packageManager)
    }
}
