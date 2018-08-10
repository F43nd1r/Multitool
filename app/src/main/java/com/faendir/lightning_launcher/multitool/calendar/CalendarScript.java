package com.faendir.lightning_launcher.multitool.calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Instances;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author lukas
 * @since 10.08.18
 */
public class CalendarScript implements JavaScript.Setup, JavaScript.Normal {
    @NonNull private final Utils utils;

    public CalendarScript(@NonNull Utils utils) {
        this.utils = utils;
    }

    @Override
    public void setup() {
        Shortcut calendar = utils.getContainer().addShortcut("", new Intent(Intent.ACTION_MAIN).setPackage("com.google.android.calendar"), 0, 0);
        calendar.getProperties()
                .edit()
                .setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false)
                .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, true)
                .setBoolean(PropertySet.ITEM_ON_GRID, false)
                .setEventHandler(PropertySet.ITEM_RESUMED, EventHandler.RUN_SCRIPT, utils.installNormalScript().getId() + "/" + CalendarScript.class.getName())
                .commit();
        calendar.setSize(500, 200);
        utils.centerOnTouch(calendar);
    }

    @Override
    public void run() {
        Log.d(MultiTool.LOG_TAG, utils.getEvent().getSource());
        updateEntries(ProxyFactory.cast(utils.getEvent().getItem(), Shortcut.class));
    }

    private void updateEntries(Shortcut calendar) {
        SharedPreferences prefs = utils.getSharedPref();
        boolean showEnd = prefs.getBoolean(utils.getString(R.string.pref_showEnd), true);
        int entryCount = prefs.getInt(utils.getString(R.string.pref_showCount), 3);
        DateFormat dateFormat = DateFormat.values()[prefs.getInt(utils.getString(R.string.pref_dateFormat), 0)];
        Set<String> calendars = prefs.getStringSet(utils.getString(R.string.pref_calendars), Collections.emptySet());
        if (calendar.getProperties().getInteger(PropertySet.SHORTCUT_LABEL_MAX_LINES) < entryCount * 2) {
            calendar.getProperties().edit().setInteger(PropertySet.SHORTCUT_LABEL_MAX_LINES, entryCount * 2).commit();
        }
        if (calendars.isEmpty()) {
            calendar.setLabel(utils.getString(R.string.text_noCalendars));
            return;
        }
        String[] projection = new String[]{Instances._ID, Instances.BEGIN, Instances.END, Instances.TITLE, Instances.ALL_DAY};
        String selection = Instances.CALENDAR_ID + " IN ('" + TextUtils.join("','", calendars) + "')";
        Uri uri = DataProvider.getContentUri(CalendarDataSource.class);
        int searchDays = 32;
        Calendar cal = Calendar.getInstance();
        List<String> entries = new ArrayList<>();
        while (entries.size() < entryCount && searchDays <= 4096) {
            long begin = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_YEAR, searchDays);
            long stop = cal.getTimeInMillis();
            try (Cursor cursor = utils.getLightningContext()
                    .getContentResolver()
                    .query(uri.buildUpon().path(uri.getPath().replaceFirst("\\*", String.valueOf(begin)).replaceFirst("\\*", String.valueOf(stop))).build(),
                            projection,
                            selection,
                            null,
                            Instances.BEGIN + " ASC")) {
                if (cursor != null) {
                    while (cursor.moveToNext() && entries.size() < entryCount) {
                        Calendar start = Calendar.getInstance();
                        start.setTimeInMillis(cursor.getLong(1));
                        Calendar end = Calendar.getInstance();
                        end.setTimeInMillis(cursor.getLong(2));
                        boolean isAllDay = cursor.getInt(4) == 1;
                        String entry = cursor.getString(3);
                        if (isAllDay) end.add(Calendar.DAY_OF_YEAR, -1);
                        boolean endsOnSame = start.get(Calendar.DAY_OF_YEAR) == end.get(Calendar.DAY_OF_YEAR);
                        entry += " " + dateFormat.formatDate(start.getTime());
                        if (!isAllDay) {
                            entry += " " + dateFormat.formatTime(start.getTime());
                        }
                        if (showEnd) {
                            if (endsOnSame) {
                                if (!isAllDay) {
                                    entry += " - " + dateFormat.formatTime(end.getTime());
                                }
                            } else {
                                entry += " - " + dateFormat.formatDate(end.getTime());
                                if (!isAllDay) {
                                    entry += " " + dateFormat.formatTime(end.getTime());
                                }
                            }
                        }
                        entries.add(entry);
                        Log.d(MultiTool.LOG_TAG, entry);
                        Log.d(MultiTool.LOG_TAG, String.valueOf(entries.size()));
                    }
                }
            }
            searchDays *= 2;
        }
        if (entries.isEmpty()) {
            calendar.setLabel(utils.getString(R.string.text_noAppointments));
        } else {
            calendar.setLabel(TextUtils.join("\n", entries));
        }
    }

    private enum DateFormat {
        EUROPE("dd. MM.", "HH:mm"),
        US("MM/dd", "hh:mm a");
        private final SimpleDateFormat date;
        private final SimpleDateFormat time;

        DateFormat(String date, String time) {
            this.date = new SimpleDateFormat(date);
            this.time = new SimpleDateFormat(time);
        }

        public String formatDate(Date format) {
            return date.format(format);
        }

        public String formatTime(Date format) {
            return time.format(format);
        }
    }
}
