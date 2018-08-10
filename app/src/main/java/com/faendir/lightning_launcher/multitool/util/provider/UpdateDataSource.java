package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

public interface UpdateDataSource extends DataSource {
    int update(@NonNull Context context, @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs);
}
