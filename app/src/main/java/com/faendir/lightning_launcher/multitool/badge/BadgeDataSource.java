package com.faendir.lightning_launcher.multitool.badge;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider;
import com.faendir.lightning_launcher.multitool.util.provider.SharedPreferencesDataSource;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
public class BadgeDataSource extends SharedPreferencesDataSource {

    @Override
    public String getPath() {
        return "badge/*";
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return super.query(context, uri, projection, selection, new String[]{context.getString(R.string.unread_prefix) + uri.getLastPathSegment()}, sortOrder);
    }

    public static int getBadgeCount(@NonNull Context context, String packageName) {
        try (Cursor cursor = context.getContentResolver().query(getContentUri(packageName),
                null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                Integer i = Utils.GSON.fromJson(cursor.getString(1), Integer.class);
                if (i != null) {
                    return i;
                }
            }
        }
        return 0;
    }

    public static void setBadgeCount(@NonNull Context context, String packageName, int count) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(context.getString(R.string.unread_prefix) + packageName, count);
        context.getContentResolver().update(getContentUri(packageName), contentValues, null, null);
    }

    public static Uri getContentUri(String packageName){
        Uri uri = DataProvider.getContentUri(BadgeDataSource.class);
        return uri.buildUpon().path(uri.getPath().replace("*", packageName)).build();
    }
}
