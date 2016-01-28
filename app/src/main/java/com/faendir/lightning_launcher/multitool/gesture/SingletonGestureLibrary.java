package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;

import com.faendir.lightning_launcher.multitool.util.FileManager;

import java.io.File;

/**
 * Created by Lukas on 26.01.2016.
 */
public final class SingletonGestureLibrary {
    private SingletonGestureLibrary() {
    }

    private static GestureLibrary global;

    public static GestureLibrary getGlobal(Context context) {
        if (global == null) {
            File file = new File(context.getFilesDir(), "gestureLibrary");
            FileManager.allowGlobalRead(file);
            global = GestureLibraries.fromFile(file);
            global.load();
        }
        return global;
    }
}
