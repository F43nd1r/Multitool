package com.faendir.lightning_launcher.multitool.calendar

import android.content.Intent
import android.provider.CalendarContract.Instances
import android.text.TextUtils
import android.util.Log
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.*
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author lukas
 * @since 10.08.18
 */
class CalendarScript(private val utils: Utils) : JavaScript.Setup, JavaScript.Normal {

    override fun setup() {
        val calendar = utils.container.addShortcut("", Intent(Intent.ACTION_MAIN).setPackage("com.google.android.calendar"), 0f, 0f)
        calendar.properties
                .edit()
                .setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false)
                .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, true)
                .setBoolean(PropertySet.ITEM_ON_GRID, false)
                .setEventHandler(PropertySet.ITEM_RESUMED, EventHandler.RUN_SCRIPT, utils.installNormalScript().id.toString() + "/" + CalendarScript::class.java.name)
                .commit()
        calendar.setSize(500f, 200f)
        utils.centerOnTouch(calendar)
    }

    override fun run() {
        Log.d(MultiTool.LOG_TAG, utils.event.source)
        updateEntries(ProxyFactory.cast(utils.event.item, Shortcut::class.java))
    }

    private fun updateEntries(calendar: Shortcut) {
        val prefs = utils.sharedPref
        val showEnd = prefs.getBoolean(utils.getString(R.string.pref_showEnd), true)
        val entryCount = prefs.getInt(utils.getString(R.string.pref_showCount), 3)
        val dateFormat = DateFormat.values()[prefs.getInt(utils.getString(R.string.pref_dateFormat), 0)]
        val calendars = prefs.getStringSet(utils.getString(R.string.pref_calendars), emptySet())!!
        if (calendar.properties.getInteger(PropertySet.SHORTCUT_LABEL_MAX_LINES) < entryCount * 2) {
            calendar.properties.edit().setInteger(PropertySet.SHORTCUT_LABEL_MAX_LINES, (entryCount * 2).toLong()).commit()
        }
        if (calendars.isEmpty()) {
            calendar.label = utils.getString(R.string.text_noCalendars)
            return
        }
        val projection = arrayOf(Instances._ID, Instances.BEGIN, Instances.END, Instances.TITLE, Instances.ALL_DAY)
        val selection = Instances.CALENDAR_ID + " IN ('" + TextUtils.join("','", calendars) + "')"
        val uri = DataProvider.getContentUri(CalendarDataSource::class.java)
        var searchDays = 32
        val cal = Calendar.getInstance()
        val entries = ArrayList<String>()
        while (entries.size < entryCount && searchDays <= 4096) {
            val begin = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, searchDays)
            val stop = cal.timeInMillis
            utils.lightningContext
                    .contentResolver
                    .query(uri.buildUpon().path(uri.path!!.replaceFirst("\\*".toRegex(), begin.toString()).replaceFirst("\\*".toRegex(), stop.toString())).build(),
                            projection,
                            selection,
                            null,
                            Instances.BEGIN + " ASC")?.use {
                        while (it.moveToNext() && entries.size < entryCount) {
                            val start = Calendar.getInstance()
                            start.timeInMillis = it.getLong(1)
                            val end = Calendar.getInstance()
                            end.timeInMillis = it.getLong(2)
                            val isAllDay = it.getInt(4) == 1
                            var entry = it.getString(3)
                            if (isAllDay) end.add(Calendar.DAY_OF_YEAR, -1)
                            val endsOnSame = start.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR)
                            entry += " " + dateFormat.formatDate(start.time)
                            if (!isAllDay) {
                                entry += " " + dateFormat.formatTime(start.time)
                            }
                            if (showEnd) {
                                if (endsOnSame) {
                                    if (!isAllDay) {
                                        entry += " - " + dateFormat.formatTime(end.time)
                                    }
                                } else {
                                    entry += " - " + dateFormat.formatDate(end.time)
                                    if (!isAllDay) {
                                        entry += " " + dateFormat.formatTime(end.time)
                                    }
                                }
                            }
                            entries.add(entry)
                            Log.d(MultiTool.LOG_TAG, entry)
                            Log.d(MultiTool.LOG_TAG, entries.size.toString())
                        }
                    }
            searchDays *= 2
        }
        if (entries.isEmpty()) {
            calendar.label = utils.getString(R.string.text_noAppointments)
        } else {
            calendar.label = TextUtils.join("\n", entries)
        }
    }

    private enum class DateFormat(date: String, time: String) {
        EUROPE("dd. MM.", "HH:mm"),
        US("MM/dd", "hh:mm a");

        private val date: SimpleDateFormat = SimpleDateFormat(date, Locale.US)
        private val time: SimpleDateFormat = SimpleDateFormat(time, Locale.US)

        fun formatDate(format: Date): String {
            return date.format(format)
        }

        fun formatTime(format: Date): String {
            return time.format(format)
        }
    }
}
