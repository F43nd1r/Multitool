package com.faendir.lightning_launcher.multitool.launcherscript

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.fastadapter.Model

/**
 * @author lukas
 * @since 04.07.18
 */
class ActionGroup(override val name: String) : Model {

    override fun getIcon(context: Context): Drawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_drop_down_white)!!

    override val tintColor= Color.BLACK
}
