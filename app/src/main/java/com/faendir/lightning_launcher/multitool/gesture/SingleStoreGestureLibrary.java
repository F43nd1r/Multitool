package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureStore;
import android.gesture.Prediction;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.faendir.lightning_launcher.multitool.util.DataProvider;

import org.acra.ACRA;

import java.io.IOException;
import java.util.ArrayList;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;
import static com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG;

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
final class SingleStoreGestureLibrary {

    private static GestureStore gestureStore;

    static synchronized SingleStoreGestureLibrary getInstance(Context context) {
        SingleStoreGestureLibrary library = new SingleStoreGestureLibrary(context);
        if (gestureStore == null) {
            if (DEBUG) Log.d(LOG_TAG, "Creating Gesture Library");
            gestureStore = new GestureStore();
            load(context);
        }
        return library;
    }

    private final Context context;

    private SingleStoreGestureLibrary(Context context) {
        this.context = context;
        if (DEBUG) Log.d(LOG_TAG, "Created Gesture Library");
    }

    boolean save() {
        return save(context);
    }

    private static synchronized boolean save(Context context) {
        if (!gestureStore.hasChanged()) return true;
        boolean result = false;
        try {
            gestureStore.save(new ParcelFileDescriptor.AutoCloseOutputStream(DataProvider.getGestureLibraryFile(context)), true);
            result = true;
        } catch (IOException e) {
            if (DEBUG) Log.d(LOG_TAG, "Could not save the gesture library", e);
            ACRA.getErrorReporter().handleSilentException(e);
        }
        return result;
    }

    private static void load(Context context) {
        try (ParcelFileDescriptor.AutoCloseInputStream in = new ParcelFileDescriptor.AutoCloseInputStream(DataProvider.getGestureLibraryFile(context))) {
            if (in.available() > 0) {
                gestureStore.load(in, false);
            }
        } catch (IOException e) {
            if (DEBUG) Log.d(LOG_TAG, "Could not load the gesture library", e);
            ACRA.getErrorReporter().handleSilentException(e);
        }
    }

    ArrayList<Prediction> recognize(Gesture gesture) {
        return gestureStore.recognize(gesture);
    }

    void addGesture(String entryName, Gesture gesture) {
        synchronized (SingleStoreGestureLibrary.class) {
            gestureStore.addGesture(entryName, gesture);
        }
    }

    void removeGesture(String entryName, Gesture gesture) {
        synchronized (SingleStoreGestureLibrary.class) {
            gestureStore.removeGesture(entryName, gesture);
        }
    }

    void removeEntry(String entryName) {
        synchronized (SingleStoreGestureLibrary.class) {
            gestureStore.removeEntry(entryName);
        }
    }

    ArrayList<Gesture> getGestures(String entryName) {
        return gestureStore.getGestures(entryName);
    }
}
