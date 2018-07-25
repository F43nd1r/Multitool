package com.faendir.lightning_launcher.multitool.drawer;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import androidx.preference.MultiSelectListPreference;
import java9.util.stream.StreamSupport;

import java.util.Collection;
import java.util.Set;

import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.exceptionToOptional;

/**
 * @author F43nd1r
 * @since 13.11.2016
 */

public class RestorePreference extends MultiSelectListPreference {
    private final PackageManager pm;

    public RestorePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(true);
        pm = getContext().getPackageManager();
    }

    private String getLabelForComponent(String flatComponent) {
        return exceptionToOptional(pm::getActivityInfo).apply(ComponentName.unflattenFromString(flatComponent), 0).map(info -> info.loadLabel(pm).toString()).orElse("");
    }

    @Override
    public boolean callChangeListener(Object newValue) {
        Set<String> values = getValues();
        //noinspection SuspiciousMethodCalls
        values.removeAll((Collection<?>) newValue);
        super.callChangeListener(values);
        setValues(values);
        return false;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        //noinspection unchecked
        Set<String> values = getPersistedStringSet((Set<String>) defaultValue);
        setEntryValues(values.toArray(new String[0]));
        setEntries(StreamSupport.stream(values).map(this::getLabelForComponent).toArray(String[]::new));
        setValues(values);
    }
}
