package com.faendir.lightning_launcher.multitool.badge;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.R;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author F43nd1r
 * @since 25.04.2017
 */
public class BadgeBroadcastReceiver extends BroadcastReceiver {
    private static final String ASUS_BADGE_COUNT = "badge_count";
    private static final String ASUS_PACKAGE_NAME = "badge_count_package_name";
    private static final String ASUS_INTENT_BADGE_COUNT_UPDATE = "android.intent.action.BADGE_COUNT_UPDATE";
    private static final String ADW_INTENT_BADGE_COUNT_UPDATE = "org.adw.launcher.counter.SEND";
    private static final String ADW_PACKAGE_NAME = "PNAME";
    private static final String ADW_BADGE_COUNT = "COUNT";
    private static final String APEX_INTENT_BADGE_COUNT_UPDATE = "com.anddoes.launcher.COUNTER_CHANGED";
    private static final String APEX_PACKAGE_NAME = "package";
    private static final String APEX_BADGE_COUNT = "count";
    private static final String HTC_INTENT_BADGE_COUNT_UPDATE = "com.htc.launcher.action.UPDATE_SHORTCUT";
    private static final String HTC_PACKAGE_NAME = "packagename";
    private static final String HTC_BADGE_COUNT = "count";
    private static final String HTC_NEW_INTENT_BADGE_COUNT_UPDATE = "com.htc.launcher.action.SET_NOTIFICATION";
    private static final String HTC_NEW_COMPONENT = "com.htc.launcher.extra.COMPONENT";
    private static final String HTC_NEW_BADGE_COUNT = "com.htc.launcher.extra.COUNT";
    private static final String SONY_INTENT_BADGE_COUNT_UPDATE = "com.sonyericsson.home.action.UPDATE_BADGE";
    private static final String SONY_PACKAGE_NAME = "com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME";
    private static final String SONY_BADGE_COUNT = "com.sonyericsson.home.intent.extra.badge.MESSAGE";
    private static final String VIVO_INTENT_BADGE_COUNT_UPDATE = "launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM";
    private static final String VIVO_PACKAGE_NAME = "packageName";
    private static final String VIVO_BADGE_COUNT = "notificationNum";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null) switch (action) {
            case ASUS_INTENT_BADGE_COUNT_UPDATE:
                if (intent.hasExtra(ASUS_BADGE_COUNT) && intent.hasExtra(ASUS_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(ASUS_PACKAGE_NAME), intent.getIntExtra(ASUS_BADGE_COUNT, 0), action);
                }
                break;
            case ADW_INTENT_BADGE_COUNT_UPDATE:
                if (intent.hasExtra(ADW_BADGE_COUNT) && intent.hasExtra(ADW_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(ADW_PACKAGE_NAME), intent.getIntExtra(ADW_BADGE_COUNT, 0), action);
                }
                break;
            case APEX_INTENT_BADGE_COUNT_UPDATE:
                if (intent.hasExtra(APEX_BADGE_COUNT) && intent.hasExtra(APEX_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(APEX_PACKAGE_NAME), intent.getIntExtra(APEX_BADGE_COUNT, 0), action);
                }
                break;
            case HTC_INTENT_BADGE_COUNT_UPDATE:
                if (intent.hasExtra(HTC_BADGE_COUNT) && intent.hasExtra(HTC_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(HTC_PACKAGE_NAME), intent.getIntExtra(HTC_BADGE_COUNT, 0), action);
                }
                break;
            case HTC_NEW_INTENT_BADGE_COUNT_UPDATE:
                if (intent.hasExtra(HTC_NEW_BADGE_COUNT) && intent.hasExtra(HTC_NEW_COMPONENT)) {
                    final ComponentName componentName = ComponentName.unflattenFromString(intent.getStringExtra(HTC_NEW_COMPONENT));
                    if (componentName != null) {
                        updateBadgeCount(context, componentName.getPackageName(), intent.getIntExtra(HTC_NEW_BADGE_COUNT, 0), action);
                    }
                }
                break;
            case SONY_INTENT_BADGE_COUNT_UPDATE:
                if (intent.hasExtra(SONY_BADGE_COUNT) && intent.hasExtra(SONY_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(SONY_PACKAGE_NAME), Integer.parseInt(intent.getStringExtra(SONY_BADGE_COUNT)), action);
                }
                break;
            case VIVO_INTENT_BADGE_COUNT_UPDATE:
                if (intent.hasExtra(VIVO_BADGE_COUNT) && intent.hasExtra(VIVO_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(VIVO_PACKAGE_NAME), intent.getIntExtra(VIVO_BADGE_COUNT, 0), action);
                }
                break;
        }
    }

    private void updateBadgeCount(Context context, String packageName, int count, String action) {
        if (MultiTool.DEBUG) Log.d(MultiTool.LOG_TAG, "Notification count for " + packageName + " updated to " + count + " by Intent " + action);
        BadgeDataSource.setBadgeCount(context, packageName, count);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> set = sharedPref.getStringSet(context.getString(R.string.key_badgeIntentPackages), Collections.emptySet());
        if (!set.contains(packageName)) {
            set = new HashSet<>(set);
            set.add(packageName);
            sharedPref.edit().putStringSet(context.getString(R.string.key_badgeIntentPackages), set).apply();
        }
    }
}
