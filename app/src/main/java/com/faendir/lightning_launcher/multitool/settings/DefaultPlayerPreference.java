package com.faendir.lightning_launcher.multitool.settings;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * @author F43nd1r
 * @since 08.11.2016
 */

public class DefaultPlayerPreference extends ListPreference implements SummaryPreference, HasPlayerEntries {

    public DefaultPlayerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        discoverPlayers(context.getPackageManager());
    }

    @Override
    public CharSequence getSummaryText() {
        return getEntry();
    }
}
