package com.faendir.lightning_launcher.multitool.event;

import androidx.annotation.IdRes;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
public class ClickEvent {
    @IdRes
    private final int id;

    public ClickEvent(@IdRes int id) {
        this.id = id;
    }

    @IdRes
    public int getId() {
        return id;
    }
}
