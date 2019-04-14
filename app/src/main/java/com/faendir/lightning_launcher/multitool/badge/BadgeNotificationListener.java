package com.faendir.lightning_launcher.multitool.badge;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.annotation.RequiresApi;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.notification.NotificationListener;
import java9.lang.Iterables;
import java9.util.stream.Collectors;
import java9.util.stream.Stream;
import java9.util.stream.StreamSupport;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            StatusBarNotification[] array = null;
            try {
                array = context.getActiveNotifications();
            } catch (RuntimeException ignored) {
            }
            if (array != null) {
                List<StatusBarNotification> notifications = Stream.of(array).filter(n -> packageName.equals(n.getPackageName())).collect(Collectors.toList());
                int reduceBy = 0;
                Map<String, Integer> groupSizes = StreamSupport.stream(notifications)
                        .collect(Collectors.toMap(StatusBarNotification::getGroupKey, n -> 1, (i1, i2) -> i1 + i2));
                Iterables.removeIf(groupSizes.entrySet(), e -> e.getValue() == 1);
                reduceBy = groupSizes.size();
                number = (int) (Stream.of(array).filter(n -> packageName.equals(n.getPackageName())).count() - reduceBy);
            }
            if (number == 0) {
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
