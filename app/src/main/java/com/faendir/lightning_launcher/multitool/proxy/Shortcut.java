package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Intent;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Shortcut extends Item {
    String getLabel();

    Intent getIntent();

    Image getDefaultIcon();

    Image getCustomIcon();
}
