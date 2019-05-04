package com.faendir.lightning_launcher.multitool.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable

import com.faendir.lightning_launcher.multitool.fastadapter.Model

import java.lang.ref.SoftReference

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
class IntentInfo internal constructor(val intent: Intent, private val provider: DrawableProvider, override val name: String, internal val isIndirect: Boolean) : Model, Comparable<IntentInfo> {
    private var icon: SoftReference<Drawable>? = null

    override val tintColor: Int
        get() = Color.WHITE

    override fun getIcon(context: Context): Drawable {
        return if (icon == null || icon!!.get() == null) {
            val drawable = provider.drawable
            icon = SoftReference(drawable)
            drawable
        } else {
            icon!!.get()!!
        }
    }

    override fun compareTo(other: IntentInfo): Int {
        return name.compareTo(other.name)
    }
}
