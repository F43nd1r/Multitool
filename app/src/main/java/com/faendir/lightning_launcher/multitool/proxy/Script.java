package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface Script extends Proxy {
    void setText(String text);

    int getId();

    String getPath();

    void run(Screen screen, String data);
}
