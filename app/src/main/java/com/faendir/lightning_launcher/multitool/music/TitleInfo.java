package com.faendir.lightning_launcher.multitool.music;

import android.graphics.Bitmap;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

public class TitleInfo {
    private final String title;
    private final String album;
    private final String artist;
    private final String packageName;
    private final Bitmap albumArt;

    public TitleInfo(String title, String album, String artist, String packageName, Bitmap albumArt) {
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.packageName = packageName;
        this.albumArt = albumArt;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getPackageName() {
        return packageName;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }
}
