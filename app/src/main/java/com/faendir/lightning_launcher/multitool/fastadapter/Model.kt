package com.faendir.lightning_launcher.multitool.fastadapter

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

/**
 * @author F43nd1r
 * @since 11.10.2017
 */

interface Model {
    val name: String
    @get:ColorInt
    val tintColor: Int

    fun getIcon(context: Context): Drawable
}
