package com.faendir.lightning_launcher.multitool.util.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.core.content.ContextCompat;
import com.faendir.lightning_launcher.multitool.R;

import java.lang.reflect.InvocationTargetException;

/**
 * @author lukas
 * @since 18.07.18
 */
public class CanDisableTimePicker extends TimePicker {
    public CanDisableTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CanDisableTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CanDisableTimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CanDisableTimePicker(Context context) {
        super(context);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        View radial = findViewById(getSystemId("radial_picker"));
        if (radial != null) {
            try {
                radial.getClass().getMethod("setInputEnabled", boolean.class).invoke(radial, enabled);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        int[] textViews = new int[]{getSystemId("hours"), getSystemId("minutes"), getSystemId("separator"), getSystemId("am_label"), getSystemId("pm_label")};
        ColorStateList colors = ContextCompat.getColorStateList(getContext(), R.color.timepicker_text_color);
        for (int id : textViews) {
            TextView view = findViewById(id);
            if (view != null) {
                view.setTextColor(colors);
            }
        }
    }

    private int getSystemId(String name) {
        return Resources.getSystem().getIdentifier(name, "id", "android");
    }
}
