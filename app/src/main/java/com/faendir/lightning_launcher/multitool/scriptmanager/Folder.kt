package com.faendir.lightning_launcher.multitool.scriptmanager

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.fastadapter.Model

/**
 * @author F43nd1r
 * @since 29.10.2016
 */

data class Folder(override var name: String) : Comparable<Folder>, Model {

    override val tintColor: Int
        get() = Color.WHITE

    override fun compareTo(other: Folder): Int = name.compareTo(other.name)

    override fun getIcon(context: Context): Drawable = ContextCompat.getDrawable(context, R.drawable.ic_folder_white)!!
}
