package com.faendir.lightning_launcher.multitool.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.SortedMap;
import java.util.TreeMap;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by Lukas on 08.11.2016.
 */

public class DefaultPlayerPreference extends ListPreference implements SummaryPreference {

    public DefaultPlayerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        PackageManager pm = context.getPackageManager();
        SortedMap<String, String> map = StreamSupport.stream(pm.queryBroadcastReceivers(new Intent(Intent.ACTION_MEDIA_BUTTON), 0))
                .map(info -> info.activityInfo).collect(Collectors.<ActivityInfo, String, String, SortedMap<String, String>>toMap(
                        info -> info.applicationInfo.loadLabel(pm).toString(), info-> info.packageName, (k1, k2) -> k1, TreeMap::new));
        setEntries(map.keySet().toArray(new String[0]));
        setEntryValues(map.values().toArray(new String[0]));
    }

    @Override
    public CharSequence getSummaryText() {
        return getEntry();
    }
}
