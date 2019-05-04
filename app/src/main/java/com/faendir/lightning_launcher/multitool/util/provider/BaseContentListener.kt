package com.faendir.lightning_launcher.multitool.util.provider

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import androidx.annotation.Keep

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
abstract class BaseContentListener(handler: Handler?, val context: Context, private val uri: Uri) : ContentObserver(handler) {

    abstract override fun onChange(selfChange: Boolean)

    open fun register() {
        onChange(false)
        context.contentResolver.registerContentObserver(uri, false, this)
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
    }
}
