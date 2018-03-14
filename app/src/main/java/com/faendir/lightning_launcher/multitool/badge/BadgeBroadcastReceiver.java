package com.faendir.lightning_launcher.multitool.badge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.faendir.lightning_launcher.multitool.R;

import java.util.HashSet;
import java.util.Set;

/**
 * @author F43nd1r
 * @since 25.04.2017
 */
public class BadgeBroadcastReceiver extends BroadcastReceiver {
    private static final String BADGE_COUNT = "badge_count";
    private static final String PACKAGE_NAME = "badge_count_package_name";
    private static final String INTENT_BADGE_COUNT_UPDATE = "android.intent.action.BADGE_COUNT_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (INTENT_BADGE_COUNT_UPDATE.equals(intent.getAction())) {
            if (intent.hasExtra(BADGE_COUNT) && intent.hasExtra(PACKAGE_NAME)) {
                String packageName = intent.getStringExtra(PACKAGE_NAME);
                int count = intent.getIntExtra(BADGE_COUNT, 0);
                BadgeDataSource.setBadgeCount(context, packageName, count);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                Set<String> set = sharedPref.getStringSet(context.getString(R.string.key_badgeIntentPackages), new HashSet<>());
                if (!set.contains(packageName)) {
                    set.add(packageName);
                    sharedPref.edit().putStringSet(context.getString(R.string.key_badgeIntentPackages), set).apply();
                }
            }
        }
    }
}
