package com.faendir.lightning_launcher.multitool.proxy

import android.content.Intent

/**
 * @author lukas
 * @since 08.07.18
 */
interface ActivityScreen : Screen {
    fun cropImage(image: ImageBitmap, full_size: Boolean): ImageBitmap

    fun hideActionBar()

    fun pickColor(title: String, color: Int, hasAlpha: Boolean): Int

    fun pickImage(maxPixels: Int): Image

    fun pickNumericValue(title: String, value: Float, valueType: String, min: Float, max: Float, interval: Float, unit: String): Float

    fun showActionBar(onCreateOptionsMenu: Function, onOptionsItemSelected: Function)

    fun startActivityForResult(intent: Intent, receiver: Script, token: String): Boolean
}
