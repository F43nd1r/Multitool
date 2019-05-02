package com.faendir.lightning_launcher.multitool.proxy

import androidx.annotation.ColorLong
import androidx.annotation.StringDef

/**
 * @author lukas
 * @since 05.07.18
 */
interface Box : Proxy {

    @get:HorizontalAlignment
    val alignmentH: String

    @get:VerticalAlignment
    val alignmentV: String

    val box: Any

    fun getColor(@Area area: String, @Mode mode: String): Int

    fun getSize(@Area area: String): Int

    fun setAlignment(@HorizontalAlignment h: String, @VerticalAlignment v: String)

    fun setColor(areas: String, modes: String, @ColorLong color: Long)

    fun setSize(areas: String, size: Int)

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(MARGIN_LEFT, MARGIN_TOP, MARGIN_RIGHT, MARGIN_BOTTOM, BORDER_LEFT, BORDER_TOP, BORDER_RIGHT, BORDER_BOTTOM, PADDING_LEFT, PADDING_TOP, PADDING_RIGHT, PADDING_BOTTOM, CONTENT)
    annotation class Area

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(MODE_NORMAL, MODE_FOCUSED, MODE_SELECTED)
    annotation class Mode

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(ALIGNMENT_LEFT, ALIGNMENT_CENTER, ALIGNMENT_RIGHT)
    annotation class HorizontalAlignment

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(ALIGNMENT_TOP, ALIGNMENT_MIDDLE, ALIGNMENT_BOTTOM)
    annotation class VerticalAlignment

    companion object {
        const val MARGIN_LEFT = "ml"
        const val MARGIN_TOP = "mt"
        const val MARGIN_RIGHT = "mr"
        const val MARGIN_BOTTOM = "mb"
        const val BORDER_LEFT = "bl"
        const val BORDER_TOP = "bt"
        const val BORDER_RIGHT = "br"
        const val BORDER_BOTTOM = "bb"
        const val PADDING_LEFT = "pl"
        const val PADDING_TOP = "pt"
        const val PADDING_RIGHT = "pr"
        const val PADDING_BOTTOM = "pb"
        const val CONTENT = "c"
        const val MODE_NORMAL = "n"
        const val MODE_FOCUSED = "f"
        const val MODE_SELECTED = "s"
        const val MODE_ALL = MODE_NORMAL + MODE_FOCUSED + MODE_SELECTED
        const val ALIGNMENT_LEFT = "LEFT"
        const val ALIGNMENT_CENTER = "CENTER"
        const val ALIGNMENT_RIGHT = "RIGHT"
        const val ALIGNMENT_TOP = "TOP"
        const val ALIGNMENT_MIDDLE = "MIDDLE"
        const val ALIGNMENT_BOTTOM = "BOTTOM"

        fun asString(@Area vararg areas: String): String = areas.joinToString(",")

        fun border(): String = asString(BORDER_LEFT, BORDER_TOP, BORDER_RIGHT, BORDER_BOTTOM)
    }
}
