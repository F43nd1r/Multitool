package com.faendir.lightning_launcher.multitool.util;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.R;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class DataProvider extends ContentProvider {

    public enum URI {
        SHARED_PREFERENCES("prefs"),
        GESTURE_LIBRARY("lib", "gestureLibrary"),
        GESTURE_INFOS("infos", "gestures");
        private final String path;
        private final String filename;

        URI(String path) {
            this(path, null);
        }

        URI(String path, String filename) {
            this.path = path;
            this.filename = filename;
        }

        private boolean isFile() {
            return filename != null;
        }

        private String getPath() {
            return path;
        }

        private String getFilename() {
            return filename;
        }

        private static URI fromOrdinal(int ordinal) {
            if (ordinal < 0) return null;
            URI[] values = values();
            return ordinal < values.length ? values[ordinal] : null;
        }
    }

    private enum Mode {
        r(ParcelFileDescriptor.MODE_READ_ONLY | ParcelFileDescriptor.MODE_CREATE),
        rwt(ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE);

        private final int constant;

        Mode(int constant) {
            this.constant = constant;
        }

        public int getConstant() {
            return constant;
        }
    }

    private static final String AUTHORITY = "com.faendir.lightning_launcher.multitool.provider";

    private final UriMatcher URI_MATCHER;
    private SharedPreferences sharedPref;

    public DataProvider() {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        for (URI uri : URI.values()) {
            URI_MATCHER.addURI(AUTHORITY, uri.getPath(), uri.ordinal());
        }
    }

    public static InputStream openFileForRead(Context context, URI uri) throws FileNotFoundException {
        if (uri.isFile()) {
            return context.getContentResolver().openInputStream(getContentUri(uri));
        }
        throw new IllegalArgumentException();
    }

    public static OutputStream openFileForWrite(Context context, URI uri) throws FileNotFoundException {
        if (uri.isFile()) {
            return context.getContentResolver().openOutputStream(getContentUri(uri), Mode.rwt.name());
        }
        throw new IllegalArgumentException();
    }

    private static Uri getContentUri(URI uri) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY).path(uri.getPath()).build();
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
        if (URI_MATCHER.match(uri) == URI.SHARED_PREFERENCES.ordinal()) {
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
        if (URI_MATCHER.match(uri) == URI.SHARED_PREFERENCES.ordinal()) {
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

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        Mode m;
        try {
            m = Mode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            m = Mode.r;
        }
        URI u = URI.fromOrdinal(URI_MATCHER.match(uri));
        if (u != null && u.isFile()) {
            return ParcelFileDescriptor.open(new File(getContext().getFilesDir(), u.getFilename()), m.getConstant());
        }
        return super.openFile(uri, mode);
    }
}
