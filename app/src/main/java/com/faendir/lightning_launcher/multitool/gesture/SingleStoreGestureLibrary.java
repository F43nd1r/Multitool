package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureStore;
import android.gesture.Prediction;
import android.util.Log;

import androidx.annotation.NonNull;
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider;

import org.acra.ACRA;

import java.io.IOException;
import java.io.InputStream;
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

    private SingleStoreGestureLibrary(@NonNull Context context) {
        this.context = context;
        if (DEBUG) Log.d(LOG_TAG, "Created Gesture Library");
    }

    void save() {
        save(context);
    }

    private static synchronized void save(@NonNull Context context) {
        if (!gestureStore.hasChanged()) return;
        try {
            gestureStore.save(DataProvider.openFileForWrite(context, GestureLibraryDataSource.class), true);
        } catch (IOException e) {
            if (DEBUG) Log.d(LOG_TAG, "Could not save the gesture library", e);
            ACRA.getErrorReporter().handleSilentException(e);
        }
    }

    private static void load(Context context) {
        try (InputStream in = DataProvider.openFileForRead(context, GestureLibraryDataSource.class)) {
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
