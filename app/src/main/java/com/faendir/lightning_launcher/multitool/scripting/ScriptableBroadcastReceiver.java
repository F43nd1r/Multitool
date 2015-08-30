package com.faendir.lightning_launcher.multitool.scripting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Lukas on 25.07.2015.
 * Class which can be used inside of Scripts without the need of Extension
 */
public class ScriptableBroadcastReceiver extends BroadcastReceiver {
    private Intent intent;
    private Runnable runnable;

    public ScriptableBroadcastReceiver() {
        intent = null;
        runnable = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.intent = intent;
        if (runnable != null) runnable.run();
    }

    @SuppressWarnings("unused")
    public void setCallback(Runnable run) {
        runnable = run;
    }

    @SuppressWarnings("unused")
    public Intent getIntent() {
        return intent;
    }
}
