package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Created by Lukas on 26.01.2016.
 */
public class IntentInfo implements ImageText{
    private final Drawable icon;
    private final Intent intent;
    private final String title;
    private final boolean isIndirect;

    public IntentInfo(Intent intent, Drawable icon, String title, boolean isIndirect) {
        this.icon = icon;
        this.intent = intent;
        this.title = title;
        this.isIndirect = isIndirect;
    }

    public Drawable getImage(Context context) {
        return icon;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getText() {
        return title;
    }

    public boolean isIndirect() {
        return isIndirect;
    }
}
