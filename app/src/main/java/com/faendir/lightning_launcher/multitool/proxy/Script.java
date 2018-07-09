package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface Script extends Proxy {
    int FLAG_DISABLED = 1;
    int FLAG_ALL = 0;
    int FLAG_APP_MENU = 2;
    int FLAG_ITEM_MENU = 4;
    int FLAG_CUSTOM_MENU = 8;

    void setText(String text);

    int getId();

    String getPath();

    void run(Screen screen, String data);

    void setName(String name);

    void setFlag(int flag, boolean on);

    boolean hasFlag(int flag);

    String getName();

    String getText();
}
