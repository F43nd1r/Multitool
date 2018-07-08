package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Context;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Screen extends Proxy {
    void runAction(int action, String data);

    float getLastTouchX();

    float getLastTouchY();

    Context getContext();
}
