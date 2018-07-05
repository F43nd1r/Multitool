package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface Lightning extends Proxy {
    Event getEvent();

    void save();

    VariableSet getVariables();
}
