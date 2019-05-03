package com.faendir.lightning_launcher.multitool.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.faendir.lightning_launcher.multitool.BuildConfig

/**
 * Created by Lukas on 07.12.2014.
 * a preference object showing the current version
 */
@Suppress("unused")
internal class VersionPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {

    init {
        summary = BuildConfig.VERSION_NAME
    }
}
