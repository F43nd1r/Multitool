package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface PropertySet extends Proxy {
    PropertyEditor edit();

    boolean getBoolean(String name);
}
