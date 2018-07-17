package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 17.07.18
 */
public interface Scriptable extends Proxy {
    Object get(String key, Scriptable start);

    void put(String key, Scriptable start, Object value);
}
