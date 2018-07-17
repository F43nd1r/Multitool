package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Intent;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Container extends Proxy {
    String TYPE_DESKTOP = "Desktop";
    String TYPE_FOLDER = "Folder";

    @Type
    String getType();

    int getId();

    String getTag();

    Item getOpener();

    int getWidth();

    int getHeight();

    RectL getBoundingBox();

    float getCellWidth();

    float getCellHeight();

    float getPositionX();

    float getPositionY();

    float getPositionScale();

    Item[] getAllItems();

    void removeItem(Item item);

    void setTag(String id, String value);

    Item getItemByName(String name);

    Panel addPanel(float x, float y, float width, float height);

    PropertySet getProperties();

    Shortcut addShortcut(String label, Intent intent, float x, float y);

    CustomView addCustomView(float x, float y);

    String getTag(String id);

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPE_DESKTOP, TYPE_FOLDER})
    @interface Type {
    }

    Scriptable getMy();
}
