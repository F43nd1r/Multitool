package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.Utils;
import java9.util.function.BiConsumer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
public class SharedPreferencesDataSource implements QueryDataSource, UpdateDataSource {
    static final String COLUMN_KEY = "key";
    static final String COLUMN_VALUE = "value";
    static final String COLUMN_TYPE = "type";

    @Override
    public String getPath() {
        return "pref";
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        MatrixCursor cursor = new MatrixCursor(new String[]{COLUMN_KEY, COLUMN_VALUE, COLUMN_VALUE});
        Map<String, ?> values = sharedPref.getAll();
        if (selectionArgs == null) {
            selectionArgs = values.keySet().toArray(new String[0]);
        }
        for (String s : selectionArgs) {
            Object value = values.get(s);
            if (value != null) {
                cursor.addRow(new Object[]{s, Utils.GSON.toJson(value), s.getClass().getName()});
            }
        }
        return cursor;
    }

    @Override
    public int update(@NonNull Context context, @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (Map.Entry<String, Object> entry : values.valueSet()) {
            if (entry.getValue() == null) {
                editor.remove(entry.getKey());
                continue;
            }
            if (setIfTypeMatch(entry, Boolean.class, editor::putBoolean)) continue;
            if (setIfTypeMatch(entry, Float.class, editor::putFloat)) continue;
            if (setIfTypeMatch(entry, Integer.class, editor::putInt)) continue;
            if (setIfTypeMatch(entry, Long.class, editor::putLong)) continue;
            try {
                String[] strings = Utils.GSON.fromJson(entry.getValue().toString(), String[].class);
                editor.putStringSet(entry.getKey(), new HashSet<>(Arrays.asList(strings)));
            } catch (Exception e) {
                editor.putString(entry.getKey(), entry.getValue().toString());
            }
        }
        editor.apply();
        return values.size();
    }

    public void init(@NonNull Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.prefs, true);
        PreferenceManager.setDefaultValues(context, R.xml.drawer, true);
        PreferenceManager.setDefaultValues(context, R.xml.backup, true);
        PreferenceManager.setDefaultValues(context, R.xml.badge, true);
        PreferenceManager.setDefaultValues(context, R.xml.calendar, true);
        if (MultiTool.DEBUG) Log.d(MultiTool.LOG_TAG, "Loaded default pref values");
    }

    private <T> boolean setIfTypeMatch(Map.Entry<String, Object> entry, Class<T> clazz, BiConsumer<String, T> consumer) {
        if (clazz.isAssignableFrom(entry.getValue().getClass())) {
            //noinspection unchecked
            consumer.accept(entry.getKey(), (T) entry.getValue());
            return true;
        }
        return false;
    }
}
