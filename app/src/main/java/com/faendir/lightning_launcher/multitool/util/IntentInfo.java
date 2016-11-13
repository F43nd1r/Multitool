package com.faendir.lightning_launcher.multitool.util;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.faendir.omniadapter.model.Leaf;

import java.lang.ref.SoftReference;

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
class IntentInfo extends Leaf implements Comparable<IntentInfo>{
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

    Drawable getImage() {
        if (icon == null || icon.get() == null) {
            Drawable drawable = provider.getDrawable();
            icon = new SoftReference<>(drawable);
            return drawable;
        } else{
            return icon.get();
        }
    }

    Intent getIntent() {
        return intent;
    }

    String getText() {
        return title;
    }

    boolean isIndirect() {
        return isIndirect;
    }

    @Override
    public int compareTo(@NonNull IntentInfo o) {
        return getText().compareTo(o.getText());
    }
}
