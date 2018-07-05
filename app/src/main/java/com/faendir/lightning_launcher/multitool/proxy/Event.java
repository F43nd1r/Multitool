package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface Event extends Proxy {
    Item getItem();

    String getSource();

    long getDate();

    Container getContainer();

    Screen getScreen();

    String getData();

    float getTouchX();

    float getTouchY();

    float getTouchScreenX();

    float getTouchScreenY();
}
