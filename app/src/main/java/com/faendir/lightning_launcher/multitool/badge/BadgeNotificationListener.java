package com.faendir.lightning_launcher.multitool.badge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.notification.NotificationListener;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java8.lang.Iterables;
import java8.util.stream.Collectors;
import java8.util.stream.RefStreams;
import java8.util.stream.StreamSupport;

/**
 * @author F43nd1r
 * @since 03.07.2016
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BadgeNotificationListener implements NotificationListener {
    private SharedPreferences sharedPref;

    @Override
    public void onCreate(NotificationListenerService context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onNotificationPosted(NotificationListenerService context, StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();
        if (supportsIntentBasedCount(context, packageName)) {
            return;
        }
        int number = sbn.getNotification().number;
        if (number == 0) {
            StatusBarNotification[] array = context.getActiveNotifications();
            if (array != null) {
                List<StatusBarNotification> notifications = RefStreams.of(array).filter(n -> packageName.equals(n.getPackageName())).collect(Collectors.toList());
                int reduceBy = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Map<String, Integer> groupSizes = StreamSupport.stream(notifications).collect(Collectors.toMap(StatusBarNotification::getGroupKey, n -> 1, (i1, i2) -> i1 + i2));
                    Iterables.removeIf(groupSizes.entrySet(), e -> e.getValue() == 1);
                    reduceBy = groupSizes.size();
                }
                number = (int) (RefStreams.of(array).filter(n -> packageName.equals(n.getPackageName())).count() - reduceBy);
            } else {
                number = 1;
            }
        }
        BadgeDataSource.setBadgeCount(context, packageName, number);
    }

    @Override
    public void onNotificationRemoved(NotificationListenerService context, StatusBarNotification sbn) {
        if (supportsIntentBasedCount(context, sbn.getPackageName())) {
            return;
        }
        BadgeDataSource.setBadgeCount(context, sbn.getPackageName(), 0);
    }

    private boolean supportsIntentBasedCount(Context context, String packageName) {
        Set<String> set = sharedPref.getStringSet(context.getString(R.string.key_badgeIntentPackages), Collections.emptySet());
        return set.contains(packageName);
    }
}