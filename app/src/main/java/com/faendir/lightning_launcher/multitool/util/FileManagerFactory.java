package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.gesture.GestureInfo;

/**
 * Created on 27.01.2016.
 *
 * @author F43nd1r
 */
public final class FileManagerFactory {
    private FileManagerFactory() {
    }

    @NonNull
    public static FileManager<GestureInfo> createGestureFileManager(Context context) {
        return new FileManager<>(() -> DataProvider.getGestureInfoFile(context), GestureInfo[].class);
    }
}
