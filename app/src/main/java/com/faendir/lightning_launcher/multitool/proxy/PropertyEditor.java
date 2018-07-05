package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface PropertyEditor extends Proxy {
    PropertyEditor setBoolean(String name, boolean value);

    void commit();
}
