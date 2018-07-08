package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Image extends Proxy {
    int getWidth();

    int getHeight();

    String getType();

    interface Class extends Proxy {
        static Class get(@NonNull Context context) {
            try {
                return ProxyFactory.lightningProxy(context.getClassLoader().loadClass("net.pierrox.lightning_launcher.script.api.Image"), Class.class);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        ImageBitmap createImage(int width, int height);

        Image createImage(String pkg, String name);
    }
}
