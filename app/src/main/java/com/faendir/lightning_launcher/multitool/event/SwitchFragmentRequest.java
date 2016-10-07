package com.faendir.lightning_launcher.multitool.event;

import android.support.annotation.StringRes;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
public class SwitchFragmentRequest {
    @StringRes
    private final int id;

    public SwitchFragmentRequest(@StringRes int id) {
        this.id = id;
    }

    @StringRes
    public int getId() {
        return id;
    }
}
