package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface VariableSet extends Proxy {
    VariableEditor edit();

    String getString(String name);
}
