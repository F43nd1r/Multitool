package com.faendir.lightning_launcher.multitool.event;

import android.support.annotation.IdRes;

/**
 * Created by Lukas on 01.04.2016.
 */
public class SwitchFragmentRequest {
    @IdRes
    private final int id;
    private final String title;

    public SwitchFragmentRequest(@IdRes int id, String title) {
        this.id = id;
        this.title = title;
    }

    @IdRes
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
