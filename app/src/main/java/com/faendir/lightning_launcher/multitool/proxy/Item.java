package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Item extends Proxy {
    String getName();

    int getId();

    String getTag();

    String getType();

    int getWidth();

    int getHeight();

    float getPositionX();

    float getPositionY();

    float getScaleX();

    float getScaleY();

    float getRotation();

    RectL getCell();

    boolean isVisible();

    Image getBoxBackground(String state);

    PropertySet getProperties();

    void setSize(float width, float height);

    Container getParent();

    void setTag(String id, String value);

    void setCell(int left, int top, int right, int bottom);

    void setPosition(float x, float y);

    void setRotation(float angle);

    void setScale(float scaleX, float scaleY);

    void setSkew(float skewX, float skewY);

    void setVisibility(boolean visible);
}
