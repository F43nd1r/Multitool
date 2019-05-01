package com.faendir.lightning_launcher.multitool.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.provider.CalendarContract.Calendars
import android.text.TextUtils
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.preference.MultiSelectListPreference
import com.faendir.lightning_launcher.multitool.settings.SummaryPreference
import java.util.*

/**
 * @author lukas
 * @since 10.08.18
 */
class CalendarPreference(context: Context, attrs: AttributeSet) : MultiSelectListPreference(context, attrs), SummaryPreference {

    private fun getSelectedEntries(): List<String> {
            val entries = entries.map { it.toString() }.toList()
            val values = entryValues.map { it.toString() }.toList()
            return getValues().asSequence().map { values.indexOf(it) }.filter { it >= 0 }.map { entries[it] }.sorted().toList()
        }

    init {
        refresh()
    }

    override fun getSummaryText(): CharSequence {
        return TextUtils.join(", ", getSelectedEntries())
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        val calendars = HashSet<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            context.contentResolver.query(Calendars.CONTENT_URI, arrayOf(Calendars._ID), Calendars.VISIBLE + " = 1", null, Calendars._ID + " ASC")?.use {
                while (it.moveToNext()) {
                    calendars.add(it.getString(0))
                }
            }
        }
        return calendars
    }

    fun refresh() {
        val calendars = HashMap<String, String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            context.contentResolver
                    .query(Calendars.CONTENT_URI, arrayOf(Calendars._ID, Calendars.NAME), Calendars.VISIBLE + " = 1", null, Calendars._ID + " ASC")?.use {
                        while (it.moveToNext()) {
                            calendars[it.getString(0)] = it.getString(1)
                        }
                    }
        }
        entryValues = calendars.keys.toTypedArray<CharSequence>()
        entries = calendars.values.toTypedArray<CharSequence>()
    }
}
