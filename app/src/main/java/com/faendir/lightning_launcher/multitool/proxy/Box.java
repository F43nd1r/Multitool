package com.faendir.lightning_launcher.multitool.proxy;

import android.support.annotation.ColorLong;
import android.support.annotation.StringDef;
import java9.util.stream.Collectors;
import java9.util.stream.Stream;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author lukas
 * @since 05.07.18
 */
public interface Box extends Proxy {
    String MARGIN_LEFT = "ml";
    String MARGIN_TOP = "mt";
    String MARGIN_RIGHT = "mr";
    String MARGIN_BOTTOM = "mb";
    String BORDER_LEFT = "bl";
    String BORDER_TOP = "bt";
    String BORDER_RIGHT = "br";
    String BORDER_BOTTOM = "bb";
    String PADDING_LEFT = "pl";
    String PADDING_TOP = "pt";
    String PADDING_RIGHT = "pr";
    String PADDING_BOTTOM = "pb";
    String CONTENT = "c";
    String MODE_NORMAL = "n";
    String MODE_FOCUSED = "f";
    String MODE_SELECTED = "s";
    String MODE_ALL = MODE_NORMAL + MODE_FOCUSED + MODE_SELECTED;

    static String asString(@Area String... areas) {
        return Stream.of(areas).collect(Collectors.joining(","));
    }

    static String border() {
        return asString(BORDER_LEFT, BORDER_TOP, BORDER_RIGHT, BORDER_BOTTOM);
    }

    void setColor(String areas, @Mode String modes, @ColorLong long color);

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({MARGIN_LEFT,
                MARGIN_TOP,
                MARGIN_RIGHT,
                MARGIN_BOTTOM,
                BORDER_LEFT,
                BORDER_TOP,
                BORDER_RIGHT,
                BORDER_BOTTOM,
                PADDING_LEFT,
                PADDING_TOP,
                PADDING_RIGHT,
                PADDING_BOTTOM,
                CONTENT})
    @interface Area {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({MODE_NORMAL, MODE_FOCUSED, MODE_SELECTED, MODE_ALL})
    @interface Mode {

    }
}
