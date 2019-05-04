package com.faendir.lightning_launcher.multitool.util.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.faendir.lightning_launcher.multitool.BuildConfig
import com.faendir.lightning_launcher.multitool.badge.BadgeDataSource
import com.faendir.lightning_launcher.multitool.calendar.CalendarDataSource
import com.faendir.lightning_launcher.multitool.gesture.GestureLibraryDataSource
import com.faendir.lightning_launcher.multitool.gesture.GestureMetaDataSource
import com.faendir.lightning_launcher.multitool.music.MusicDataSource
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

/**
 * Provides various data (including SharedPreferences) to LL
 *
 * @author F43nd1r
 * @since 15.08.2015
 */
class DataProvider : ContentProvider() {

    private val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    enum class Mode(val constant: Int) {
        r(ParcelFileDescriptor.MODE_READ_ONLY or ParcelFileDescriptor.MODE_CREATE),
        rwt(ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE)
    }

    init {
        for (i in DATA_SOURCES.indices) {
            val dataSource = DATA_SOURCES[i]
            uriMatcher.addURI(AUTHORITY, dataSource.path, i)
        }
    }

    override fun onCreate(): Boolean {
        DATA_SOURCES.forEach { dataSource -> dataSource.init(context!!) }
        return true

    }

    private inline fun <reified T : DataSource> uriToSource(uri: Uri): T? {
        return uriMatcher.match(uri).let { if (it != UriMatcher.NO_MATCH) DATA_SOURCES[it].let { dataSource -> if (dataSource is T) dataSource else null } else null }
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return uriToSource<QueryDataSource>(uri)?.query(context!!, uri, projection, selection, selectionArgs, sortOrder)
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        try {
            return uriToSource<UpdateDataSource>(uri)?.update(context!!, uri, values, selection, selectionArgs) ?: 0
        } finally {
            context!!.contentResolver.notifyChange(uri, null)
        }
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val m = Mode.values().firstOrNull { it.name == mode } ?: Mode.r
        val dataSource = uriToSource<FileDataSource>(uri)
        return if (dataSource != null) {
            ParcelFileDescriptor.open(dataSource.getFile(context!!), m.constant)
        } else {
            super.openFile(uri, mode)
        }
    }

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"
        val DATA_SOURCES = listOf(
                SharedPreferencesDataSource(),
                GestureLibraryDataSource(),
                GestureMetaDataSource(),
                MusicDataSource(),
                BadgeDataSource(),
                CalendarDataSource())

        @Throws(FileNotFoundException::class)
        inline fun <reified T : DataSource> openFileForRead(context: Context): InputStream? {
            return context.contentResolver.openInputStream(getContentUri<T>())
        }

        @Throws(FileNotFoundException::class)
        inline fun <reified T : DataSource> openFileForWrite(context: Context): OutputStream? {
            return context.contentResolver.openOutputStream(getContentUri<T>(), Mode.rwt.name)
        }

        inline fun <reified T : DataSource> getContentUri(): Uri {
            return DATA_SOURCES.firstOrNull { it is T }?.let { dataSource -> Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY).path(dataSource.path).build() }
                    ?: throw IllegalArgumentException()
        }
    }
}
