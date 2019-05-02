package com.faendir.lightning_launcher.multitool.music

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.faendir.lightning_launcher.multitool.util.Utils
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider
import com.faendir.lightning_launcher.multitool.util.provider.FileDataSource
import com.faendir.lightning_launcher.multitool.util.provider.SharedPreferencesDataSource
import java.io.File

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

class MusicDataSource : SharedPreferencesDataSource(), FileDataSource {

    override fun getPath(): String = "music"

    override fun query(context: Context, uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return super.query(context, uri, projection, selection, KEYS, sortOrder)
    }

    override fun getFile(context: Context): File = File(context.filesDir, "albumart.png")

    companion object {
        private const val KEY_TITLE = "music_title"
        private const val KEY_ARTIST = "music_artist"
        private const val KEY_ALBUM = "music_album"
        private const val KEY_PACKAGE = "music_package"
        private val KEYS = arrayOf(KEY_TITLE, KEY_ARTIST, KEY_ALBUM, KEY_PACKAGE)

        fun updateInfo(context: Context, info: TitleInfo) {
            val contentValues = ContentValues()
            contentValues.put(KEY_TITLE, info.title)
            contentValues.put(KEY_ARTIST, info.artist)
            contentValues.put(KEY_ALBUM, info.album)
            contentValues.put(KEY_PACKAGE, info.packageName)
            if (info.albumArt != null) {
                try {
                    DataProvider.openFileForWrite(context, MusicDataSource::class.java).use { outputStream -> info.albumArt.compress(Bitmap.CompressFormat.PNG, 100, outputStream) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            context.contentResolver.update(DataProvider.getContentUri(MusicDataSource::class.java), contentValues, null, null)
        }

        fun queryInfo(context: Context): TitleInfo {
            lateinit var title: String
            lateinit var album: String
            lateinit var artist: String
            lateinit var packageName: String
            context.contentResolver.query(DataProvider.getContentUri(MusicDataSource::class.java), null, null, null, null)
                    ?.use { cursor ->
                        while (cursor.moveToNext()) {
                            when (cursor.getString(0)) {
                                KEY_TITLE -> title = Utils.GSON.fromJson(cursor.getString(1), String::class.java)
                                KEY_ALBUM -> album = Utils.GSON.fromJson(cursor.getString(1), String::class.java)
                                KEY_ARTIST -> artist = Utils.GSON.fromJson(cursor.getString(1), String::class.java)
                                KEY_PACKAGE -> packageName = Utils.GSON.fromJson(cursor.getString(1), String::class.java)
                            }
                        }
                    }
            var albumArt: Bitmap? = null
            try {
                DataProvider.openFileForRead(context, MusicDataSource::class.java).use { inputStream -> albumArt = BitmapFactory.decodeStream(inputStream) }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return TitleInfo(title, album, artist, packageName, albumArt)
        }
    }
}
