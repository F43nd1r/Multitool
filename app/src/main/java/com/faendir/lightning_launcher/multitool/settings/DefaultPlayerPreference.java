package com.faendir.lightning_launcher.multitool.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Lukas on 08.11.2016.
 */

public class DefaultPlayerPreference extends ListPreference implements SummaryPreference {

    public DefaultPlayerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        PackageManager pm = context.getPackageManager();
        Intent baseIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        SortedMap<String, String> map = new TreeMap<>();
        List<ResolveInfo> infos = pm.queryBroadcastReceivers(baseIntent, 0);
        for (ResolveInfo info : infos) {
            ActivityInfo activityInfo = info.activityInfo;
            Intent intent = new Intent(baseIntent);
            intent.setClassName(activityInfo.packageName, activityInfo.name);
            map.put(activityInfo.applicationInfo.loadLabel(pm).toString(), intent.toUri(0));
        }
        setEntries(map.keySet().toArray(new String[0]));
        setEntryValues(map.values().toArray(new String[0]));
    }

    @Override
    public CharSequence getSummaryText() {
        return getEntry();
    }
}
