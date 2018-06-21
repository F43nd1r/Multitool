package com.faendir.lightning_launcher.multitool.fastadapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

/**
 * @author F43nd1r
 * @since 11.10.2017
 */

public interface Model {
    String getName();
    Drawable getIcon(@NonNull Context context);
    @ColorInt int getTintColor();
}
