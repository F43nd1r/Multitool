package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

public interface FileDataSource extends DataSource {
    File getFile(@NonNull Context context);
}
