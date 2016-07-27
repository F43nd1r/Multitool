package com.faendir.lightning_launcher.multitool.event;


import android.support.v7.view.ActionMode;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
public class UpdateActionModeRequest {
    private final ActionMode.Callback callback;
    private final boolean show;

    public UpdateActionModeRequest(ActionMode.Callback callback, boolean show) {
        this.callback = callback;
        this.show = show;
    }

    public ActionMode.Callback getCallback() {
        return callback;
    }

    public boolean show() {
        return show;
    }
}
