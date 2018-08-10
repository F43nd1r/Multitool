package com.faendir.lightning_launcher.multitool.calendar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.provider.CalendarContract.Calendars;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.core.content.ContextCompat;
import androidx.preference.MultiSelectListPreference;
import com.faendir.lightning_launcher.multitool.settings.SummaryPreference;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lukas
 * @since 10.08.18
 */
public class CalendarPreference extends MultiSelectListPreference implements SummaryPreference {
    public CalendarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        refresh();
    }

    private List<CharSequence> getSelectedEntries() {
        List<CharSequence> entries = Arrays.asList(getEntries());
        List<CharSequence> values = Arrays.asList(getEntryValues());
        return StreamSupport.stream(getValues()).map(values::indexOf).filter(index -> index >= 0).map(entries::get).sorted().collect(Collectors.toList());
    }

    @Override
    public CharSequence getSummaryText() {
        return TextUtils.join(", ", getSelectedEntries());
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        Set<String> calendars = new HashSet<>();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            try (Cursor cursor = getContext().getContentResolver().query(Calendars.CONTENT_URI, new String[]{Calendars._ID}, Calendars.VISIBLE + " = 1", null, Calendars._ID + " ASC")) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        calendars.add(cursor.getString(0));
                    }
                }
            }
        }
        return calendars;
    }

    public void refresh() {
        Map<String, String> calendars = new HashMap<>();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            try (Cursor cursor = getContext().getContentResolver()
                    .query(Calendars.CONTENT_URI, new String[]{Calendars._ID, Calendars.NAME}, Calendars.VISIBLE + " = 1", null, Calendars._ID + " ASC")) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        calendars.put(cursor.getString(0), cursor.getString(1));
                    }
                }
            }
        }
        setEntryValues(calendars.keySet().toArray(new CharSequence[0]));
        setEntries(calendars.values().toArray(new CharSequence[0]));
    }
}
