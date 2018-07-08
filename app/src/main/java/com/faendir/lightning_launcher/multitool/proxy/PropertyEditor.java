package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface PropertyEditor extends Proxy {
    PropertyEditor setBoolean(String name, boolean value);

    void commit();

    Box getBox(String name);

    PropertyEditor setEventHandler(String name, int action, String data);

    PropertyEditor setString(String name, String value);

    PropertyEditor setInteger(String name, long value);
}
