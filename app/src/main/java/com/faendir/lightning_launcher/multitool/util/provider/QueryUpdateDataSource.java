package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

public interface QueryUpdateDataSource extends DataSource {
    @Nullable
    Cursor query(@NonNull Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

    int update(@NonNull Context context, @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs);
}
