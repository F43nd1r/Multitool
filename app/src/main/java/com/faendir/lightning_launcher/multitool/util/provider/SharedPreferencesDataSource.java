package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.google.gson.JsonSyntaxException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import java8.util.function.BiConsumer;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

public class SharedPreferencesDataSource implements QueryUpdateDataSource {
    private boolean init = false;

    @Override
    public String getPath() {
        return "pref";
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        init(context);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        MatrixCursor cursor = new MatrixCursor(new String[]{"key", "value"});
        Map<String, ?> values = sharedPref.getAll();
        for (String s : selectionArgs) {
            cursor.addRow(new Object[]{s, Utils.GSON.toJson(values.get(s))});
        }
        return cursor;
    }

    @Override
    public int update(@NonNull Context context, @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        init(context);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (Map.Entry<String, Object> entry : values.valueSet()) {
            if (setIfTypeMatch(entry, Boolean.class, editor::putBoolean)) continue;
            if (setIfTypeMatch(entry, Float.class, editor::putFloat)) continue;
            if (setIfTypeMatch(entry, Integer.class, editor::putInt)) continue;
            if (setIfTypeMatch(entry, Long.class, editor::putLong)) continue;
            try {
                String[] strings = Utils.GSON.fromJson(entry.getValue().toString(), String[].class);
                editor.putStringSet(entry.getKey(), new HashSet<>(Arrays.asList(strings)));
            } catch (JsonSyntaxException e) {
                editor.putString(entry.getKey(), entry.getValue().toString());
            }
        }
        editor.apply();
        return values.size();
    }

    protected void init(@NonNull Context context) {
        if (!init) {
            PreferenceManager.setDefaultValues(context, R.xml.prefs, true);
            PreferenceManager.setDefaultValues(context, R.xml.drawer, true);
            init = true;
        }
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
