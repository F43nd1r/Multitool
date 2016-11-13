package com.faendir.lightning_launcher.multitool.util;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.R;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import java8.util.function.BiConsumer;

/**
 * Provides various data (including SharedPreferences) to LL
 *
 * @author F43nd1r
 * @since 15.08.2015
 */
public class PreferenceProvider extends ContentProvider {

    private static final String AUTHORITY = "com.faendir.lightning_launcher.multitool.provider";
    private static final int SHARED_PREFERENCES = 1;

    private final UriMatcher URI_MATCHER;
    private SharedPreferences sharedPref;

    public PreferenceProvider() {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, "pref", SHARED_PREFERENCES);
    }

    @Override
    public boolean onCreate() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        PreferenceManager.setDefaultValues(getContext(), R.xml.prefs, true);
        PreferenceManager.setDefaultValues(getContext(), R.xml.drawer, true);
        return true;

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (URI_MATCHER.match(uri) == SHARED_PREFERENCES) {
            MatrixCursor cursor = new MatrixCursor(new String[]{"key", "value"});
            Map<String, ?> values = sharedPref.getAll();
            for (String s : selectionArgs) {
                cursor.addRow(new Object[]{s, Utils.GSON.toJson(values.get(s))});
            }
            return cursor;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (URI_MATCHER.match(uri) == SHARED_PREFERENCES) {
            SharedPreferences.Editor editor = sharedPref.edit();
            for (Map.Entry<String, Object> entry : values.valueSet()) {
                if (setIfTypeMatch(entry, Boolean.class, editor::putBoolean)) continue;
                if (setIfTypeMatch(entry, Float.class, editor::putFloat)) continue;
                if (setIfTypeMatch(entry, Integer.class, editor::putInt)) continue;
                if (setIfTypeMatch(entry, Long.class, editor::putLong)) continue;
                try {
                    String[] strings = Utils.GSON.fromJson(entry.getValue().toString(), String[].class);
                    editor.putStringSet(entry.getKey(), new HashSet<>(Arrays.asList(strings)));
                }catch (JsonSyntaxException e){
                    editor.putString(entry.getKey(), entry.getValue().toString());
                }
            }
            editor.apply();
        }
        return values.size();
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
