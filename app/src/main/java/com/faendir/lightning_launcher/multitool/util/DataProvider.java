package com.faendir.lightning_launcher.multitool.util;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.gesture.GestureInfo;
import com.faendir.lightning_launcher.multitool.gesture.SingletonGestureLibrary;
import com.google.gson.JsonSyntaxException;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java8.util.function.BiConsumer;
import java8.util.stream.StreamSupport;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;
import static com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG;

/**
 * Provides various data (including SharedPreferences) to LL
 *
 * @author F43nd1r
 * @since 15.08.2015
 */
public class DataProvider extends ContentProvider {

    private static final String AUTHORITY = "com.faendir.lightning_launcher.multitool.provider";
    private static final String LIBRARY = "lib";
    private static final String INFOS = "infos";
    private static final int SHARED_PREFERENCES = 1;
    private static final int GESTURE_LIBRARY = 2;
    private static final int GESTURE_INFOS = 3;

    private final UriMatcher URI_MATCHER;
    private SharedPreferences sharedPref;

    public DataProvider() {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, "pref", SHARED_PREFERENCES);
        URI_MATCHER.addURI(AUTHORITY, LIBRARY, GESTURE_LIBRARY);
        URI_MATCHER.addURI(AUTHORITY, INFOS, GESTURE_INFOS);
    }

    public static List<GestureInfo> getGestureInfos(Context context) {
        List<GestureInfo> gestureInfos = new ArrayList<>();
        try (Cursor cursor = context.getContentResolver().query(getContentUri(INFOS), null, null, null, null)) {
            while (cursor.moveToNext()) {
                try {
                    gestureInfos.add(new GestureInfo(Intent.parseUri(cursor.getString(0), 0), cursor.getString(1), ParcelUuid.fromString(cursor.getString(2))));
                } catch (URISyntaxException ignored) {
                }
            }
            cursor.close();
        }
        return gestureInfos;
    }

    public static FileDescriptor getGestureLibraryFile(Context context) {
        try {
            return context.getContentResolver().openFileDescriptor(getContentUri(DataProvider.LIBRARY), "").getFileDescriptor();
        } catch (FileNotFoundException e) {
            if (DEBUG) Log.d(LOG_TAG, "Did not find gesture file");
            return new FileDescriptor();
        }
    }

    private static Uri getContentUri(String suffix) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY).path(suffix).build();
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
                    cursor.addRow(new Object[]{s, Utils.GSON.toJson(values.get(s))});
                }
                return cursor;
            }
            case GESTURE_INFOS: {
                MatrixCursor cursor = new MatrixCursor(new String[]{"intent", "label", "uuid"});
                FileManager<GestureInfo> fileManager = FileManagerFactory.createGestureFileManager(getContext());
                StreamSupport.stream(fileManager.read()).forEach(info -> cursor.addRow(new Object[]{info.getIntent().toUri(0), info.getLabel(), info.getUuid().toString()}));
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
        if (URI_MATCHER.match(uri) == GESTURE_LIBRARY) {
            return ParcelFileDescriptor.open(SingletonGestureLibrary.getFile(getContext()), ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE);
        }
        return super.openFile(uri, mode);
    }
}
