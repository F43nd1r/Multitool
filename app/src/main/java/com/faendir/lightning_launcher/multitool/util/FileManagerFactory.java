package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;

import com.faendir.lightning_launcher.multitool.gesture.Gesture;
import com.faendir.lightning_launcher.multitool.scriptmanager.ScriptGroup;

/**
 * Created by Lukas on 27.01.2016.
 */
public final class FileManagerFactory {
    private FileManagerFactory() {
    }

    public static FileManager<ScriptGroup> createScriptFileManager(Context context){
        return new FileManager<>(context, "storage", ScriptGroup[].class);
    }

    public static FileManager<Gesture> createGestureFileManager(Context context){
        return new FileManager<>(context, "gestures", Gesture[].class);
    }
}
