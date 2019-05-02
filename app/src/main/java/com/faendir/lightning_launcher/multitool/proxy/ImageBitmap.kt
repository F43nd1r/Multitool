package com.faendir.lightning_launcher.multitool.proxy

import android.graphics.Bitmap
import android.graphics.Canvas

/**
 * @author lukas
 * @since 04.07.18
 */
interface ImageBitmap : Image {

    val bitmap: Bitmap

    fun draw(): Canvas
}
