package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Intent;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Container extends Proxy {
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
}
