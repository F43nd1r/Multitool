package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import androidx.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.util.provider.FileDataSource;

import java.io.File;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

public class GestureLibraryDataSource implements FileDataSource {

    @Override
    public String getPath() {
        return "lib";
    }

    @Override
    public File getFile(@NonNull Context context) {
        return new File(context.getFilesDir(), "gestureLibrary");
    }
}
