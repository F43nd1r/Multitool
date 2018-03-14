package com.faendir.lightning_launcher.multitool.music;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider;
import com.faendir.lightning_launcher.multitool.util.provider.FileDataSource;
import com.faendir.lightning_launcher.multitool.util.provider.SharedPreferencesDataSource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

public class MusicDataSource extends SharedPreferencesDataSource implements FileDataSource {
    private static final String KEY_TITLE = "music_title";
    private static final String KEY_ARTIST = "music_artist";
    private static final String KEY_ALBUM = "music_album";
    private static final String KEY_PACKAGE = "music_package";
    private static final String[] KEYS = {KEY_TITLE, KEY_ARTIST, KEY_ALBUM, KEY_PACKAGE};

    @Override
    public String getPath() {
        return "music";
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return super.query(context, uri, projection, selection, KEYS, sortOrder);
    }

    @Override
    public File getFile(@NonNull Context context) {
        return new File(context.getFilesDir(), "albumart.png");
    }

    public static void updateInfo(@NonNull Context context, TitleInfo info) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TITLE, info.getTitle());
        contentValues.put(KEY_ARTIST, info.getArtist());
        contentValues.put(KEY_ALBUM, info.getAlbum());
        contentValues.put(KEY_PACKAGE, info.getPackageName());
        if(info.getAlbumArt() != null) {
            try (OutputStream outputStream = DataProvider.openFileForWrite(context, MusicDataSource.class)) {
                info.getAlbumArt().compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        context.getContentResolver().update(DataProvider.getContentUri(MusicDataSource.class), contentValues, null, null);
    }

    public static TitleInfo queryInfo(@NonNull Context context) {
        String title = null;
        String album = null;
        String artist = null;
        String packageName = null;
        Cursor cursor = context.getContentResolver().query(DataProvider.getContentUri(MusicDataSource.class), null, null, null, null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                switch (cursor.getString(0)) {
                    case KEY_TITLE:
                        title = Utils.GSON.fromJson(cursor.getString(1), String.class);
                        break;
                    case KEY_ALBUM:
                        album = Utils.GSON.fromJson(cursor.getString(1), String.class);
                        break;
                    case KEY_ARTIST:
                        artist = Utils.GSON.fromJson(cursor.getString(1), String.class);
                        break;
                    case KEY_PACKAGE:
                        packageName = Utils.GSON.fromJson(cursor.getString(1), String.class);
                        break;
                }
            }
            cursor.close();
        }
        Bitmap albumArt = null;
        try (InputStream inputStream = DataProvider.openFileForRead(context, MusicDataSource.class)) {
            albumArt = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new TitleInfo(title, album, artist, packageName, albumArt);
    }
}
