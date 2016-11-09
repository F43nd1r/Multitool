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

    private static SingletonGestureLibrary global;

    public static GestureLibrary getGlobal(Context context) {
        if (global == null) {
            global = new SingletonGestureLibrary(context);
        }
        return global.get();
    }

    public static File getFile(){
        return global.file;
    }

    private final GestureLibrary library;
    private final File file;
    private long lastModified = 0;

    private SingletonGestureLibrary(Context context) {
        file = new File(context.getFilesDir(), "gestureLibrary");
        FileManager.allowGlobalRead(file);
        library = GestureLibraries.fromFile(file);
    }

    private GestureLibrary get() {
        if (file.lastModified() > lastModified) {
            lastModified = file.lastModified();
            library.load();
        }
        return library;
    }
}
