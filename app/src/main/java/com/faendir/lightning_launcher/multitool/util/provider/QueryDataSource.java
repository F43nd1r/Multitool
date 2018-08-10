package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author lukas
 * @since 10.08.18
 */
public interface QueryDataSource extends DataSource {
    @Nullable
    Cursor query(@NonNull Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);
}
