package com.faendir.lightning_launcher.multitool.event;

import androidx.annotation.StringRes;
import com.faendir.lightning_launcher.multitool.util.Fragments;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
public class SwitchFragmentRequest {
    private final Fragments fragment;

    public SwitchFragmentRequest(@StringRes int id) {
        this(Fragments.stream().filter(f -> f.getRes() == id).findAny().orElseThrow());
    }

    public SwitchFragmentRequest(Fragments fragment) {
        this.fragment = fragment;
    }

    public Fragments getFragment() {
        return fragment;
    }

    @StringRes
    public int getId() {
        return fragment.getRes();
    }
}
