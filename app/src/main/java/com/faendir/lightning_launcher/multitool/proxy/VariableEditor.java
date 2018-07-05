package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface VariableEditor extends Proxy {
    VariableEditor setString(String name, String value);

    void commit();
}
