package com.faendir.lightning_launcher.multitool.util

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

/**
 * Created on 21.03.2016.
 *
 * @author F43nd1r
 */
class ResolveInfoDrawableProvider(private val packageManager: PackageManager, private val info: ResolveInfo) : DrawableProvider {

    override val drawable: Drawable
        get() = info.loadIcon(packageManager)
}
