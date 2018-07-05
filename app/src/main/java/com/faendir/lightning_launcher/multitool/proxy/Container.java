package com.faendir.lightning_launcher.multitool.proxy;

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
}
