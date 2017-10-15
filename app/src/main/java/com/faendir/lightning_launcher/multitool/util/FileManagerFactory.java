package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.gesture.GestureInfo;

import java.io.FileNotFoundException;

/**
 * Created on 27.01.2016.
 *
 * @author F43nd1r
 */
public final class FileManagerFactory {
    private FileManagerFactory() {
    }

    @NonNull
    public static FileManager<GestureInfo, FileNotFoundException> createGestureFileManager(Context context) {
        return new FileManager<>(() -> DataProvider.openFileForRead(context, DataProvider.URI.GESTURE_INFOS), () -> DataProvider.openFileForWrite(context, DataProvider.URI.GESTURE_INFOS), GestureInfo[].class);
    }
}
