package com.faendir.lightning_launcher.multitool.launcherscript;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.Model;

/**
 * @author lukas
 * @since 04.07.18
 */
public class ActionGroup implements Model {
    private final String name;

    public ActionGroup(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Drawable getIcon(@NonNull Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_arrow_drop_down_white);
    }

    @Override
    public int getTintColor() {
        return Color.BLACK;
    }
}
