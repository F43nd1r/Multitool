package com.faendir.lightning_launcher.multitool.util;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

/**
 * Created by Lukas on 21.03.2016.
 */
public class ResolveInfoDrawableProvider implements DrawableProvider {

    private final PackageManager packageManager;
    private final ResolveInfo info;

    public ResolveInfoDrawableProvider(PackageManager packageManager, ResolveInfo info) {
        this.packageManager = packageManager;
        this.info = info;
    }

    @Override
    public Drawable getDrawable() {
        return info.loadIcon(packageManager);
    }
}
