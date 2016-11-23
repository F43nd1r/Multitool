package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.gesture.GestureLibrary;
import android.util.Log;

import com.faendir.lightning_launcher.multitool.util.DataProvider;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;
import static com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG;

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
public final class SingletonGestureLibrary extends GestureLibrary {

    private static SingletonGestureLibrary global;

    static GestureLibrary getGlobal(Context context) {
        if (global == null) {
            if (DEBUG) Log.d(LOG_TAG, "Creating Gesture Library");
            global = new SingletonGestureLibrary(context);
        }
        return global.get();
    }

    public static File getFile(Context context) {
        return new File(context.getFilesDir(), "gestureLibrary");
    }

    private final FileDescriptor file;

    private SingletonGestureLibrary(Context context) {
        file = DataProvider.getGestureLibraryFile(context);
        if (DEBUG) Log.d(LOG_TAG, "Created Gesture Library");
    }

    private GestureLibrary get() {
        load();
        return this;
    }

    public boolean save() {
        if (!mStore.hasChanged()) return true;

        boolean result = false;
        try {
            mStore.save(new FileOutputStream(file), true);
            result = true;
        } catch (IOException e) {
            if(DEBUG) Log.d(LOG_TAG, "Could not save the gesture library in " + file, e);
        }
        return result;
    }

    public boolean load() {
        boolean result = false;
        try {
            mStore.load(new FileInputStream(file), true);
            result = true;
        } catch (IOException e) {
            if(DEBUG) Log.d(LOG_TAG, "Could not load the gesture library from " + file, e);
        }
        return result;
    }
}
