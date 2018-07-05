package com.faendir.lightning_launcher.multitool.proxy;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface ImageBitmap extends Image {

    Bitmap getBitmap();

    Canvas draw();
}
