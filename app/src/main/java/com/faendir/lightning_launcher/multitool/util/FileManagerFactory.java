package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;

import com.faendir.lightning_launcher.multitool.gesture.GestureInfo;
import com.faendir.lightning_launcher.multitool.scriptmanager.ScriptGroup;
import com.faendir.lightning_launcher.multitool.viewcreator.CustomView;

/**
 * Created by Lukas on 27.01.2016.
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

    public static FileManager<CustomView> createCustomViewFileManager(Context context) {
        return new FileManager<>(context, "views", CustomView[].class);
    }
}
