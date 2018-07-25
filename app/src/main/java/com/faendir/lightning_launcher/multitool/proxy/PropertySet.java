package com.faendir.lightning_launcher.multitool.proxy;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface PropertySet extends Proxy {
    String PAUSED = "paused";
    String RESUMED = "resumed";
    String ITEM_PAUSED = "i.paused";
    String ITEM_RESUMED = "i.resumed";
    String ITEM_MENU = "i.menu";
    String ITEM_TAP = "i.tap";
    String POSITION_CHANGED = "posChanged";
    String SHORTCUT_LABEL_VISIBILITY = "s.labelVisibility";
    String SHORTCUT_ICON_VISIBILITY = "s.iconVisibility";
    String ITEM_ENABLED = "i.enabled";
    String ITEM_ON_GRID = "i.onGrid";
    String ITEM_SELECTION_EFFECT = "i.selectionEffect";
    String ITEM_SELECTION_EFFECT_PLAIN = "PLAIN";
    String VIEW_ON_CREATE = "v.onCreate";
    String ITEM_PIN_MODE = "i.pinMode";
    String SCROLLING_DIRECTION = "scrollingDirection";
    String SCROLLING_DIRECTION_NONE = "NONE";
    String GRID_LANDSCAPE_COLUMN_NUM = "gridLColumnNum";
    String GRID_LANDSCAPE_ROW_NUM = "gridLRowNum";
    String GRID_PORTRAIT_COLUMN_NUM = "gridPColumnNum";
    String GRID_PORTRAIT_ROW_NUM = "gridPRowNum";
    String SHORTCUT_LABEL = "s.label";
    String ITEM_BOX = "i.box";

    PropertyEditor edit();

    boolean getBoolean(@BooleanProperty String name);

    EventHandler getEventHandler(@EventProperty String name);

    String getString(@StringProperty String name);

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({PAUSED, RESUMED, ITEM_PAUSED, ITEM_RESUMED, ITEM_MENU, ITEM_TAP, POSITION_CHANGED})
    @interface EventProperty {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SHORTCUT_LABEL_VISIBILITY, SHORTCUT_ICON_VISIBILITY, ITEM_ENABLED, ITEM_ON_GRID})
    @interface BooleanProperty {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ITEM_SELECTION_EFFECT, VIEW_ON_CREATE, SCROLLING_DIRECTION, ITEM_PIN_MODE})
    @interface StringProperty {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({GRID_LANDSCAPE_COLUMN_NUM, GRID_LANDSCAPE_ROW_NUM, GRID_PORTRAIT_COLUMN_NUM, GRID_PORTRAIT_ROW_NUM})
    @interface IntProperty {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SHORTCUT_LABEL})
    @interface BindingProperty {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ITEM_BOX})
    @interface BoxProperty {
    }
}
