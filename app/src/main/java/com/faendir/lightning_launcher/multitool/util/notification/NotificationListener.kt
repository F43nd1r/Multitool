package com.faendir.lightning_launcher.multitool.util.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

interface NotificationListener {

    fun onCreate(context: NotificationListenerService) {}

    fun onNotificationPosted(context: NotificationListenerService, sbn: StatusBarNotification) {}

    fun onNotificationRemoved(context: NotificationListenerService, sbn: StatusBarNotification) {}
}
