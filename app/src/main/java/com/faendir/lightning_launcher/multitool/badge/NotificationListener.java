package com.faendir.lightning_launcher.multitool.badge;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.scriptlib.DialogActivity;

import java.util.Collections;
import java.util.Set;

/**
 * @author F43nd1r
 * @since 03.07.2016
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {
    private SharedPreferences sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (supportsIntentBasedCount(sbn.getPackageName())) {
            return;
        }
        int number = sbn.getNotification().number;
        if (number == 0) {
            number = 1;
        }
        BadgeDataSource.setBadgeCount(this, sbn.getPackageName(), number);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (supportsIntentBasedCount(sbn.getPackageName())) {
            return;
        }
        BadgeDataSource.setBadgeCount(this, sbn.getPackageName(), 0);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        onNotificationRemoved(sbn);
    }

    private boolean supportsIntentBasedCount(String packageName) {
        Set<String> set = sharedPref.getStringSet(getString(R.string.key_badgeIntentPackages), Collections.emptySet());
        return set.contains(packageName);
    }

    public static boolean isEnabled(@NonNull Context context) {
        ComponentName notificationListener = new ComponentName(context, NotificationListener.class);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(notificationListener.flattenToString());
    }

    public static void askForEnable(@NonNull Context context) {
        new DialogActivity.Builder(context, R.style.AppTheme_Dialog_Alert)
                .setTitle(R.string.title_listener)
                .setMessage(R.string.text_listener)
                .setButtons(android.R.string.yes, android.R.string.no, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                        if (resultCode == AlertDialog.BUTTON_POSITIVE) {
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }
                })
                .show();
    }
}
