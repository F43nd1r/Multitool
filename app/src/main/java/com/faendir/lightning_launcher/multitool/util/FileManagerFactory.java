package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;

import com.faendir.lightning_launcher.multitool.gesture.GestureInfo;
import com.faendir.lightning_launcher.multitool.scriptmanager.ScriptGroup;

/**
 * Created on 27.01.2016.
 *
 * @author F43nd1r
 */
public final class FileManagerFactory {
    private FileManagerFactory() {
    }

    public static FileManager<ScriptGroup> createScriptFileManager(Context context) {
        return new FileManager<>(context, "storage", ScriptGroup[].class);
    }

    public static FileManager<GestureInfo> createGestureFileManager(Context context) {
        return new FileManager<>(context, "gestures", GestureInfo[].class);
    }
}
