package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Image extends Proxy {
    int getWidth();

    int getHeight();

    String getType();

    interface Class extends Proxy {
        ImageBitmap createImage(int width, int height);
    }
}
