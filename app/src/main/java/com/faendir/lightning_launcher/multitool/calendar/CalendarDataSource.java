package com.faendir.lightning_launcher.multitool.calendar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.faendir.lightning_launcher.multitool.util.provider.QueryDataSource;

import java.util.List;

/**
 * @author lukas
 * @since 10.08.18
 */
public class CalendarDataSource implements QueryDataSource {
    @Nullable
    @Override
    public Cursor query(@NonNull Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            List<String> pathSegments = uri.getPathSegments();
            return context.getContentResolver()
                    .query(CalendarContract.Instances.CONTENT_URI.buildUpon()
                            .appendPath(pathSegments.get(pathSegments.size() - 2))
                            .appendPath(pathSegments.get(pathSegments.size() - 1))
                            .build(), projection, selection, selectionArgs, sortOrder);
        }
        return null;
    }

    @Override
    public String getPath() {
        return "calendar/*/*";
    }
}
