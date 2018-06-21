package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.fastadapter.Model;

import java.lang.ref.SoftReference;

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
public class IntentInfo implements Model, Comparable<IntentInfo>{
    private SoftReference<Drawable> icon;
    private final DrawableProvider provider;
    private final Intent intent;
    private final String title;
    private final boolean isIndirect;

    IntentInfo(Intent intent, DrawableProvider provider, String title, boolean isIndirect) {
        this.provider = provider;
        this.intent = intent;
        this.title = title;
        this.isIndirect = isIndirect;

    }

    @Override
    public Drawable getIcon(@NonNull Context context) {
        if (icon == null || icon.get() == null) {
            Drawable drawable = provider.getDrawable();
            icon = new SoftReference<>(drawable);
            return drawable;
        } else{
            return icon.get();
        }
    }

    public Intent getIntent() {
        return intent;
    }

    @Override
    public String getName() {
        return title;
    }

    boolean isIndirect() {
        return isIndirect;
    }

    @Override
    public int compareTo(@NonNull IntentInfo o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public int getTintColor() {
        return Color.WHITE;
    }
}
