package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface LL extends Proxy {
    Event getEvent();

    void save();

    ImageBitmap createImage(int width, int height);

    VariableSet getVariables();
}
