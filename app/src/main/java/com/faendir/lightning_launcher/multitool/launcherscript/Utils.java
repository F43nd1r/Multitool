package com.faendir.lightning_launcher.multitool.launcherscript;

import android.content.Context;

import java.io.InputStream;

/**
 * Created by TrianguloY on 09/07/2015.
 * Utilities
 */
final class Utils {
    private Utils() {
    }

    //returns a string with the resource specified
    static String getStringFromResource(int resourceId, Context context, String errorString) {

        try {
            InputStream stream = context.getResources().openRawResource(resourceId);

            byte[] b = new byte[stream.available()];
            stream.read(b);
            return new String(b);
        } catch (Exception e) {
            // e.printStackTrace();
            return errorString;
        }
    }

}
