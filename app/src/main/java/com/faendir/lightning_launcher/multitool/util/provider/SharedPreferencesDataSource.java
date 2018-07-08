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
import java9.util.function.BiConsumer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
public class SharedPreferencesDataSource implements QueryUpdateDataSource {
    private static final String COLUMN_KEY = "key";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_TYPE = "type";
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
        init(context);
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

    private void init(@NonNull Context context) {
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

    public static class Remote implements SharedPreferences {
        private final Context context;
        private Uri uri;

        public Remote(@NonNull Context context) {
            this.context = context;
            uri = DataProvider.getContentUri(SharedPreferencesDataSource.class);
        }

        @Override
        public Map<String, ?> getAll() {
            Map<String, Object> result = new HashMap<>();
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        try {
                            result.put(cursor.getString(cursor.getColumnIndex(COLUMN_KEY)),
                                    Utils.GSON.fromJson(cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)),
                                            Class.forName(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)))));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return result;
        }

        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            return get(key, defValue, String.class);
        }

        @Nullable
        @Override
        public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
            String[] result = get(key, null, String[].class);
            return result == null ? defValues : new LinkedHashSet<>(Arrays.asList(result));
        }

        @Override
        public int getInt(String key, int defValue) {
            return get(key, defValue, int.class);
        }

        @Override
        public long getLong(String key, long defValue) {
            return get(key, defValue, long.class);
        }

        @Override
        public float getFloat(String key, float defValue) {
            return get(key, defValue, float.class);
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return get(key, defValue, boolean.class);
        }

        @Override
        public boolean contains(String key) {
            return getString(key, null) == null;
        }

        @Override
        public Editor edit() {
            throw new UnsupportedOperationException("Remote SharedPreferences can't be edited");
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            throw new UnsupportedOperationException("Remote SharedPreferences do not support listeners");
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            throw new UnsupportedOperationException("Remote SharedPreferences do not support listeners");
        }

        private <T> T get(String key, T defValue, Class<T> type) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, new String[]{key}, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return Utils.GSON.fromJson(cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)), type);
                }
            }
            return defValue;
        }
    }
}
