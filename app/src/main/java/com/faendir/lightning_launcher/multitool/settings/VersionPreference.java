package com.faendir.lightning_launcher.multitool.settings;

import android.content.Context;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.faendir.lightning_launcher.multitool.BuildConfig;

/**
 * Created by Lukas on 07.12.2014.
 * a preference object showing the current version
 */
class VersionPreference extends Preference {

    public VersionPreference(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        setSummary(BuildConfig.VERSION_NAME);
    }
}
