package com.faendir.lightning_launcher.multitool.proxy;

import android.support.annotation.Nullable;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface Lightning extends Proxy {
    Event getEvent();

    void save();

    VariableSet getVariables();

    @Nullable
    Script getScriptByPathAndName(String path, String name);

    Script createScript(String path, String name, String text, int flags);

    Screen getActiveScreen();
}
