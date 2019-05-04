package com.faendir.lightning_launcher.multitool.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.faendir.lightning_launcher.multitool.util.provider.QueryDataSource

/**
 * @author lukas
 * @since 10.08.18
 */
class CalendarDataSource : QueryDataSource {
    override fun query(context: Context, uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            val pathSegments = uri.pathSegments
            return context.contentResolver.query(CalendarContract.Instances.CONTENT_URI.buildUpon()
                            .appendPath(pathSegments[pathSegments.size - 2])
                            .appendPath(pathSegments[pathSegments.size - 1])
                            .build(), projection, selection, selectionArgs, sortOrder)
        }
        return null
    }

    override val path = "calendar/*/*"
}
