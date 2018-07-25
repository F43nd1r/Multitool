package com.faendir.lightning_launcher.multitool.launcherscript;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.faendir.lightning_launcher.multitool.fastadapter.ClickAwareModel;

/**
 * @author lukas
 * @since 04.07.18
 */
public class Action implements ClickAwareModel {
    private final String name;
    private final Runnable onClick;

    public Action(String name, Runnable onClick) {
        this.name = name;
        this.onClick = onClick;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Drawable getIcon(@NonNull Context context) {
        return new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent));
    }

    @Override
    public int getTintColor() {
        return Color.WHITE;
    }


    @Override
    public void onClick() {
        onClick.run();
    }
}
