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

import java.util.Map;

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
        switch (URI_MATCHER.match(uri)) {
            case SHARED_PREFERENCES: {
                MatrixCursor cursor = new MatrixCursor(new String[]{"key", "value"});
                Map<String, ?> values = sharedPref.getAll();
                for (String s : selectionArgs) {
                    cursor.addRow(new Object[]{s, values.get(s)});
                }
                return cursor;
            }
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
        return 0;
    }
}
