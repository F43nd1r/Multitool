package com.faendir.lightning_launcher.multitool.drawer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import java8.util.stream.StreamSupport;

/**
 * Created by Lukas on 13.11.2016.
 */

public class RestorePreference extends MultiSelectListPreference {
    private final PackageManager pm;

    public RestorePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(true);
        pm = getContext().getPackageManager();
    }

    private String getLabelForComponent(String flatComponent) {
        try {
            return pm.getActivityInfo(ComponentName.unflattenFromString(flatComponent), 0).loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    @Override
    protected boolean callChangeListener(Object newValue) {
        Set<String> values = getValues();
        //noinspection SuspiciousMethodCalls
        values.removeAll((Collection<?>) newValue);
        super.callChangeListener(values);
        setValues(values);
        return false;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        Set<String> values = getSharedPreferences().getStringSet(getKey(), Collections.emptySet());
        setEntryValues(values.toArray(new String[values.size()]));
        setEntries(StreamSupport.stream(values).map(this::getLabelForComponent).toArray(String[]::new));
        setValues(Collections.emptySet());
        super.onPrepareDialogBuilder(builder);
        setValues(values);
    }
}
