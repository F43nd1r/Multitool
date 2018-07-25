package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
public interface DataSource {
    String getPath();

    default void init(@NonNull Context context) {
    }
}
