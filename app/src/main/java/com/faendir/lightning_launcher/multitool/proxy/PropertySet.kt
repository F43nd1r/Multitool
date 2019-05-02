package com.faendir.lightning_launcher.multitool.proxy

import androidx.annotation.StringDef

/**
 * @author lukas
 * @since 04.07.18
 */
interface PropertySet : Proxy {

    fun edit(): PropertyEditor

    fun getBoolean(@BooleanProperty name: String): Boolean

    fun getEventHandler(@EventProperty name: String): EventHandler?

    fun getString(@StringProperty name: String): String

    fun getInteger(@IntProperty name: String): Int

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(PAUSED, RESUMED, ITEM_PAUSED, ITEM_RESUMED, ITEM_MENU, ITEM_TAP, POSITION_CHANGED)
    annotation class EventProperty

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(SHORTCUT_LABEL_VISIBILITY, SHORTCUT_ICON_VISIBILITY, ITEM_ENABLED, ITEM_ON_GRID)
    annotation class BooleanProperty

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(ITEM_SELECTION_EFFECT, VIEW_ON_CREATE, SCROLLING_DIRECTION, ITEM_PIN_MODE)
    annotation class StringProperty

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(GRID_LANDSCAPE_COLUMN_NUM, GRID_LANDSCAPE_ROW_NUM, GRID_PORTRAIT_COLUMN_NUM, GRID_PORTRAIT_ROW_NUM, SHORTCUT_LABEL_MAX_LINES)
    annotation class IntProperty

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(SHORTCUT_LABEL)
    annotation class BindingProperty

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(ITEM_BOX)
    annotation class BoxProperty

    companion object {
        const val PAUSED = "paused"
        const val RESUMED = "resumed"
        const val ITEM_PAUSED = "i.paused"
        const val ITEM_RESUMED = "i.resumed"
        const val ITEM_MENU = "i.menu"
        const val ITEM_TAP = "i.tap"
        const val POSITION_CHANGED = "posChanged"
        const val SHORTCUT_LABEL_VISIBILITY = "s.labelVisibility"
        const val SHORTCUT_ICON_VISIBILITY = "s.iconVisibility"
        const val SHORTCUT_LABEL_MAX_LINES = "s.labelMaxLines"
        const val ITEM_ENABLED = "i.enabled"
        const val ITEM_ON_GRID = "i.onGrid"
        const val ITEM_SELECTION_EFFECT = "i.selectionEffect"
        const val ITEM_SELECTION_EFFECT_PLAIN = "PLAIN"
        const val VIEW_ON_CREATE = "v.onCreate"
        const val ITEM_PIN_MODE = "i.pinMode"
        const val SCROLLING_DIRECTION = "scrollingDirection"
        const val SCROLLING_DIRECTION_NONE = "NONE"
        const val GRID_LANDSCAPE_COLUMN_NUM = "gridLColumnNum"
        const val GRID_LANDSCAPE_ROW_NUM = "gridLRowNum"
        const val GRID_PORTRAIT_COLUMN_NUM = "gridPColumnNum"
        const val GRID_PORTRAIT_ROW_NUM = "gridPRowNum"
        const val SHORTCUT_LABEL = "s.label"
        const val ITEM_BOX = "i.box"
    }
}
