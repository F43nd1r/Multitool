package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.faendir.lightning_launcher.multitool.util.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author lukas
 * @since 08.07.18
 */
public class RemoteSharedPreferences implements SharedPreferences {
    private final Context context;
    private final Uri uri;

    public RemoteSharedPreferences(@NonNull Context context) {
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
                        result.put(cursor.getString(cursor.getColumnIndex(SharedPreferencesDataSource.COLUMN_KEY)),
                                Utils.GSON.fromJson(cursor.getString(cursor.getColumnIndex(SharedPreferencesDataSource.COLUMN_VALUE)),
                                        Class.forName(cursor.getString(cursor.getColumnIndex(SharedPreferencesDataSource.COLUMN_TYPE)))));
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
        return new Editor();
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
                return Utils.GSON.fromJson(cursor.getString(cursor.getColumnIndex(SharedPreferencesDataSource.COLUMN_VALUE)), type);
            }
        }
        return defValue;
    }

    private class Editor implements SharedPreferences.Editor {
        private final Map<String, Object> changes;

        private Editor() {
            changes = new HashMap<>();
        }

        @Override
        public SharedPreferences.Editor putString(String key, @Nullable String value) {
            changes.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, @Nullable Set<String> values) {
            changes.put(key, values);
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            changes.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            changes.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            changes.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            changes.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            changes.put(key, null);
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            for (String key : getAll().keySet()) {
                remove(key);
            }
            return this;
        }

        @Override
        public boolean commit() {
            ContentValues contentValues = new ContentValues();
            for (Map.Entry<String, Object> entry : changes.entrySet()) {
                contentValues.put(entry.getKey(), Utils.GSON.toJson(entry.getValue()));
            }
            context.getContentResolver().update(uri, contentValues, null, null);
            return true;
        }

        @Override
        public void apply() {
            new Thread(this::commit).start();
        }
    }
}
