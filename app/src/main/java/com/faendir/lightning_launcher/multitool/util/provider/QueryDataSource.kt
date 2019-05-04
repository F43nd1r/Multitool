package com.faendir.lightning_launcher.multitool.util.provider

import android.content.Context
import android.database.Cursor
import android.net.Uri

/**
 * @author lukas
 * @since 10.08.18
 */
interface QueryDataSource : DataSource {
    fun query(context: Context, uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor?
}
