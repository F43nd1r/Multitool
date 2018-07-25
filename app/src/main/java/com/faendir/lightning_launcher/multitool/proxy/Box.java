package com.faendir.lightning_launcher.multitool.proxy;

import androidx.annotation.ColorLong;
import androidx.annotation.StringDef;
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
    String ALIGNMENT_LEFT = "LEFT";
    String ALIGNMENT_CENTER = "CENTER";
    String ALIGNMENT_RIGHT = "RIGHT";
    String ALIGNMENT_TOP = "TOP";
    String ALIGNMENT_MIDDLE = "MIDDLE";
    String ALIGNMENT_BOTTOM = "BOTTOM";

    static String asString(@Area String... areas) {
        return Stream.of(areas).collect(Collectors.joining(","));
    }

    static String border() {
        return asString(BORDER_LEFT, BORDER_TOP, BORDER_RIGHT, BORDER_BOTTOM);
    }

    @HorizontalAlignment
    String getAlignmentH();

    @VerticalAlignment
    String getAlignmentV();

    Object getBox();

    int getColor(@Area String area, @Mode String mode);

    int getSize(@Area String area);

    void setAlignment(@HorizontalAlignment String h, @VerticalAlignment String v);

    void setColor(String areas, String modes, @ColorLong long color);

    void setSize(String areas, int size);

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
    @StringDef({MODE_NORMAL, MODE_FOCUSED, MODE_SELECTED})
    @interface Mode {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ALIGNMENT_LEFT, ALIGNMENT_CENTER, ALIGNMENT_RIGHT})
    @interface HorizontalAlignment {
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ALIGNMENT_TOP, ALIGNMENT_MIDDLE, ALIGNMENT_BOTTOM})
    @interface VerticalAlignment {
    }
}
