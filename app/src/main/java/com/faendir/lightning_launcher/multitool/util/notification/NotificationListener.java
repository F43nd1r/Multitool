package com.faendir.lightning_launcher.multitool.util.notification;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public interface NotificationListener {

    default void onCreate(NotificationListenerService context) {
    }

    default void onNotificationPosted(NotificationListenerService context, StatusBarNotification sbn) {
    }

    default void onNotificationRemoved(NotificationListenerService context, StatusBarNotification sbn) {
    }
}
