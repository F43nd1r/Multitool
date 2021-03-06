package com.faendir.lightning_launcher.multitool.launcherscript

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.faendir.lightning_launcher.multitool.fastadapter.ClickAwareModel

/**
 * @author lukas
 * @since 04.07.18
 */
class Action(override val name: String, private val onClick: () -> Unit) : ClickAwareModel {
    override fun getIcon(context: Context): Drawable = ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent))

    override val tintColor = Color.WHITE

    override fun onClick() = onClick.invoke()
}
