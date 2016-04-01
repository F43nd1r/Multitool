package com.faendir.lightning_launcher.multitool.event;

import android.support.annotation.IdRes;

/**
 * Created by Lukas on 01.04.2016.
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
