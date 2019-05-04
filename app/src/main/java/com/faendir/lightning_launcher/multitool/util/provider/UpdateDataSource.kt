package com.faendir.lightning_launcher.multitool.util.provider

import android.content.ContentValues
import android.content.Context
import android.net.Uri

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

interface UpdateDataSource : DataSource {
    fun update(context: Context, uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int
}
