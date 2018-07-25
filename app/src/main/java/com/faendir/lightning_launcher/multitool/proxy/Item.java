package com.faendir.lightning_launcher.multitool.proxy;

import androidx.annotation.StringDef;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Item extends Proxy {
    String TYPE_SHORTCUT = "Shortcut";
    String TYPE_FOLDER = "Folder";
    String TYPE_PANEL = "Panel";

    String getName();

    void setName(String name);

    int getId();

    String getTag();

    String getTag(String id);

    @Type
    String getType();

    int getWidth();

    int getHeight();

    float getPositionX();

    float getPositionY();

    float getScaleX();

    float getScaleY();

    float getRotation();

    void setRotation(float angle);

    RectL getCell();

    boolean isVisible();

    Image getBoxBackground(@Box.Mode String state);

    PropertySet getProperties();

    void setSize(float width, float height);

    Container getParent();

    void setTag(String id, String value);

    void setCell(int left, int top, int right, int bottom);

    void setCell(int left, int top, int right, int bottom, boolean portrait);

    void setPosition(float x, float y);

    void setScale(float scaleX, float scaleY);

    void setSkew(float skewX, float skewY);

    void setVisibility(boolean visible);

    void setBoxBackground(Image image, @Box.Mode String state, boolean persistent);

    void setBinding(@PropertySet.BindingProperty String target, String formula, boolean enabled);

    View getRootView();

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPE_SHORTCUT, TYPE_FOLDER, TYPE_PANEL})
    @interface Type {
    }
}
