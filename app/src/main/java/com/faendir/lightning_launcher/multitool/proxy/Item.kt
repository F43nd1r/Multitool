package com.faendir.lightning_launcher.multitool.proxy

import android.view.View
import androidx.annotation.StringDef

/**
 * @author lukas
 * @since 04.07.18
 */
interface Item : Proxy {

    var name: String

    val id: Int

    val tag: String

    @get:Type
    val type: String

    val width: Int

    val height: Int

    val positionX: Float

    val positionY: Float

    val scaleX: Float

    val scaleY: Float

    var rotation: Float

    val cell: RectL

    val isVisible: Boolean

    val properties: PropertySet

    val parent: Container

    val rootView: View

    fun getTag(id: String): String?

    fun getBoxBackground(@Box.Mode state: String): Image

    fun setSize(width: Float, height: Float)

    fun setTag(id: String, value: String?)

    fun setCell(left: Int, top: Int, right: Int, bottom: Int)

    fun setCell(left: Int, top: Int, right: Int, bottom: Int, portrait: Boolean)

    fun setPosition(x: Float, y: Float)

    fun setScale(scaleX: Float, scaleY: Float)

    fun setSkew(skewX: Float, skewY: Float)

    fun setVisibility(visible: Boolean)

    fun setBoxBackground(image: Image, @Box.Mode state: String, persistent: Boolean)

    fun setBinding(@PropertySet.BindingProperty target: String, formula: String, enabled: Boolean)

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(TYPE_SHORTCUT, TYPE_FOLDER, TYPE_PANEL)
    annotation class Type

    companion object {
        const val TYPE_SHORTCUT = "Shortcut"
        const val TYPE_FOLDER = "Folder"
        const val TYPE_PANEL = "Panel"
    }
}
