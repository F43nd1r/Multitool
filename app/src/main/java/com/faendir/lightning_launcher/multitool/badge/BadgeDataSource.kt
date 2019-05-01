package com.faendir.lightning_launcher.multitool.badge

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.util.Utils
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider
import com.faendir.lightning_launcher.multitool.util.provider.SharedPreferencesDataSource

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
class BadgeDataSource : SharedPreferencesDataSource() {

    override fun getPath(): String = "badge/*"

    override fun query(context: Context, uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return super.query(context, uri, projection, selection, arrayOf(context.getString(R.string.unread_prefix) + uri.lastPathSegment!!), sortOrder)
    }

    companion object {

        fun getBadgeCount(context: Context, packageName: String): Int {
            context.contentResolver.query(getContentUri(packageName), null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val i = Utils.GSON.fromJson(cursor.getString(1), Int::class.java)
                    if (i != null) {
                        return i
                    }
                }
            }
            return 0
        }

        fun setBadgeCount(context: Context, packageName: String, count: Int) {
            val contentValues = ContentValues()
            contentValues.put(context.getString(R.string.unread_prefix) + packageName, count)
            context.contentResolver.update(getContentUri(packageName), contentValues, null, null)
        }

        fun getContentUri(packageName: String): Uri {
            val uri = DataProvider.getContentUri(BadgeDataSource::class.java)

            return uri.buildUpon().path(uri.path!!.replace("*", packageName)).build()
        }
    }
}
