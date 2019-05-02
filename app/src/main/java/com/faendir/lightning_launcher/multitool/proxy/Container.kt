package com.faendir.lightning_launcher.multitool.proxy

import android.content.Intent
import androidx.annotation.StringDef

/**
 * @author lukas
 * @since 04.07.18
 */
interface Container : Proxy {

    @get:Type
    val type: String

    val id: Int

    val tag: String

    val opener: Item

    val width: Int

    val height: Int

    val boundingBox: RectL

    val cellWidth: Float

    val cellHeight: Float

    val positionX: Float

    val positionY: Float

    val positionScale: Float

    val allItems: Array<Item>

    val properties: PropertySet

    val my: Scriptable

    fun removeItem(item: Item)

    fun setTag(id: String, value: String?)

    fun getItemByName(name: String): Item

    fun addPanel(x: Float, y: Float, width: Float, height: Float): Panel

    fun addShortcut(label: String, intent: Intent, x: Float, y: Float): Shortcut

    fun addCustomView(x: Float, y: Float): CustomView

    fun getTag(id: String): String?

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(TYPE_DESKTOP, TYPE_FOLDER)
    annotation class Type

    companion object {
        const val TYPE_DESKTOP = "Desktop"
        const val TYPE_FOLDER = "Folder"
    }
}
