package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Intent;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Shortcut extends Item {
    String getLabel();

    void setLabel(String label);

    Intent getIntent();

    Image getDefaultIcon();

    Image getCustomIcon();

    void setDefaultIcon(Image image);
}
