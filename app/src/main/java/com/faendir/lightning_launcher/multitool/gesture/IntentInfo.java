package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.faendir.lightning_launcher.multitool.util.ImageText;

import java.lang.ref.WeakReference;

/**
 * Created by Lukas on 26.01.2016.
 */
public class IntentInfo implements ImageText {
    private WeakReference<Drawable> icon;
    private DrawableProvider provider;
    private final Intent intent;
    private final String title;
    private final boolean isIndirect;

    public IntentInfo(Intent intent, DrawableProvider provider, String title, boolean isIndirect) {
        this.provider = provider;
        this.intent = intent;
        this.title = title;
        this.isIndirect = isIndirect;

    }

    public Drawable getImage(Context context) {
        if (icon == null) {
            Drawable drawable = provider.getDrawable();
            icon = new WeakReference<>(drawable);
            return drawable;
        } else{
            return icon.get();
        }
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
