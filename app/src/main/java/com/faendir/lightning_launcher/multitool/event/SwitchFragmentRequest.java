package com.faendir.lightning_launcher.multitool.event;

import android.support.annotation.IdRes;

/**
 * Created by Lukas on 01.04.2016.
 */
public class SwitchFragmentRequest {
    @IdRes
    private final int id;

    public SwitchFragmentRequest(@IdRes int id) {
        this.id = id;
    }

    @IdRes
    public int getId() {
        return id;
    }
}
