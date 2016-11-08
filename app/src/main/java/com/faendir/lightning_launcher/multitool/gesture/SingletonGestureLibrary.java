package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;

import com.faendir.lightning_launcher.multitool.util.FileManager;

import java.io.File;

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
final class SingletonGestureLibrary {
    private SingletonGestureLibrary() {
    }

    private static GestureLibrary global;
    private static long lastModified = 0;

    public static GestureLibrary getGlobal(Context context) {
        File file = new File(context.getFilesDir(), "gestureLibrary");
        if (global == null) {
            FileManager.allowGlobalRead(file);
            global = GestureLibraries.fromFile(file);
        }
        if(file.lastModified() > lastModified) {
            lastModified = file.lastModified();
            global.load();
        }
        return global;
    }
}
