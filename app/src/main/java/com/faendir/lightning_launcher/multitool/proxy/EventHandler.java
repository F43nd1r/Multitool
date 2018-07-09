package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface EventHandler extends Proxy {
    int RUN_SCRIPT = 35;
    int RESTART = 28;
    int UNSET = 0;

    int getAction();

    String getData();
}
