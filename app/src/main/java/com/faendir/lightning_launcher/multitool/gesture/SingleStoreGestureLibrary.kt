package com.faendir.lightning_launcher.multitool.gesture

import android.content.Context
import android.gesture.Gesture
import android.gesture.GestureStore
import android.gesture.Prediction
import android.util.Log
import com.faendir.lightning_launcher.multitool.MultiTool.DEBUG
import com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider
import org.acra.ACRA
import java.io.IOException
import java.util.*

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
internal class SingleStoreGestureLibrary private constructor(private val context: Context) {

    init {
        if (DEBUG) Log.d(LOG_TAG, "Created Gesture Library")
    }

    fun save() {
        save(context)
    }

    fun recognize(gesture: Gesture): ArrayList<Prediction> {
        return gestureStore.recognize(gesture)
    }

    fun addGesture(entryName: String, gesture: Gesture) {
        synchronized(SingleStoreGestureLibrary::class.java) {
            gestureStore.addGesture(entryName, gesture)
        }
    }

    fun removeGesture(entryName: String, gesture: Gesture) {
        synchronized(SingleStoreGestureLibrary::class.java) {
            gestureStore.removeGesture(entryName, gesture)
        }
    }

    fun removeEntry(entryName: String) {
        synchronized(SingleStoreGestureLibrary::class.java) {
            gestureStore.removeEntry(entryName)
        }
    }

    fun getGestures(entryName: String): ArrayList<Gesture>? {
        return gestureStore.getGestures(entryName)
    }

    companion object {

        private lateinit var gestureStore: GestureStore

        @Synchronized
        fun getInstance(context: Context): SingleStoreGestureLibrary {
            val library = SingleStoreGestureLibrary(context)
            if (!::gestureStore.isInitialized) {
                if (DEBUG) Log.d(LOG_TAG, "Creating Gesture Library")
                gestureStore = GestureStore()
                load(context)
            }
            return library
        }

        @Synchronized
        private fun save(context: Context) {
            if (!gestureStore.hasChanged()) return
            try {
                gestureStore.save(DataProvider.openFileForWrite(context, GestureLibraryDataSource::class.java), true)
            } catch (e: IOException) {
                if (DEBUG) Log.d(LOG_TAG, "Could not save the gesture library", e)
                ACRA.getErrorReporter().handleSilentException(e)
            }

        }

        private fun load(context: Context) {
            try {
                DataProvider.openFileForRead(context, GestureLibraryDataSource::class.java).use {
                    if (it.available() > 0) {
                        gestureStore.load(it, false)
                    }
                }
            } catch (e: IOException) {
                if (DEBUG) Log.d(LOG_TAG, "Could not load the gesture library", e)
                ACRA.getErrorReporter().handleSilentException(e)
            }

        }
    }
}
