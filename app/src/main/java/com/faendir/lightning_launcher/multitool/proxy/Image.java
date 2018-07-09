package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Image extends Proxy {
    String TYPE_BITMAP = "BITMAP";
    int getWidth();

    int getHeight();

    @Type
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

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPE_BITMAP})
    @interface Type {
    }
}
