package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface EventHandler extends Proxy {
    int RUN_SCRIPT = 35;
    int RESTART = 28;
    int UNSET = 0;

    static EventHandler newInstance(@NonNull Context context, int action, String data) {
        try {
            return ProxyFactory.lightningProxy(context.getClassLoader()
                    .loadClass("net.pierrox.lightning_launcher.script.api.EventHandler")
                    .getConstructor(int.class, String.class)
                    .newInstance(action, data), EventHandler.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    int getAction();

    String getData();

    void setNext(EventHandler next);
}
