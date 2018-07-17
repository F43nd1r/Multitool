package com.faendir.lightning_launcher.multitool.proxy;

import android.view.View;

/**
 * @author lukas
 * @since 08.07.18
 */
public interface Menu extends Proxy {
    int MODE_ITEM_NO_EM = 12;
    int MODE_ITEM_EM = 2;

    int getMode();

    void close();

    View addMainItem(String text, Function action);
}
