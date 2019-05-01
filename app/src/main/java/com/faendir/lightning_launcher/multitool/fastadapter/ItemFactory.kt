package com.faendir.lightning_launcher.multitool.fastadapter

import android.app.ActivityManager
import android.content.Context

/**
 * @author F43nd1r
 * @since 26.12.2017
 */

class ItemFactory<T : Model>(private val size: Int) {
    fun wrap(item: T): ExpandableItem<T> = ExpandableItem(item, size)

    companion object {
        fun <T : Model> forLauncherIconSize(context: Context): ItemFactory<T> = ItemFactory((context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).launcherLargeIconSize)
    }
}
