package com.faendir.lightning_launcher.multitool;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Lukas on 08.06.2016.
 */
public class ToStringBuilder {

    private final StringBuilder builder;
    private boolean isFirstAttribute;

    public ToStringBuilder(String name) {
        builder = new StringBuilder();
        builder.append(name).append('{');
        isFirstAttribute = true;
    }

    public ToStringBuilder(Object object) {
        this(object.getClass().getSimpleName());
    }

    public ToStringBuilder append(String name, Object attribute) {
        if (isFirstAttribute) isFirstAttribute = false;
        else builder.append(",\n");
        builder.append(name).append('=');
        String s;
        if (attribute instanceof Collection) {
            s = Arrays.deepToString(((Collection) attribute).toArray());
        } else {
            s = attribute.toString();
        }
        builder.append(s.replaceAll("\\n", "\\n    "));
        return this;
    }

    public String build() {
        return builder.append('}').toString();
    }
}