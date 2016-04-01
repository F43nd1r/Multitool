package com.faendir.lightning_launcher.multitool.event;

import android.content.Intent;

/**
 * Created by Lukas on 01.04.2016.
 */
public class IntentEvent {
    private final Intent intent;

    public IntentEvent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }
}
