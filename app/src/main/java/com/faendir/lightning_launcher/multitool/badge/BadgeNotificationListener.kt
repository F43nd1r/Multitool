package com.faendir.lightning_launcher.multitool.badge

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.util.notification.NotificationListener
import java9.lang.Iterables

/**
 * @author F43nd1r
 * @since 03.07.2016
 */
class BadgeNotificationListener : NotificationListener {
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(context: NotificationListenerService) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onNotificationPosted(context: NotificationListenerService, sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (supportsIntentBasedCount(context, packageName)) {
            return
        }
        var number = sbn.notification.number
        if (number == 0) {
            val array = try {
                context.activeNotifications
            } catch (ignored: RuntimeException) {
                null
            }

            if (array != null) {
                val notifications = array.filter { packageName == it.packageName }.toList()
                val groupSizes = notifications.groupBy { it.groupKey }.mapValues { it.value.size }.toMutableMap()
                Iterables.removeIf(groupSizes.entries) { it.value == 1 }
                number = notifications.count() - groupSizes.size
            }
            if (number == 0) {
                number = 1
            }
        }
        BadgeDataSource.setBadgeCount(context, packageName, number)
    }

    override fun onNotificationRemoved(context: NotificationListenerService, sbn: StatusBarNotification) {
        if (supportsIntentBasedCount(context, sbn.packageName)) {
            return
        }
        BadgeDataSource.setBadgeCount(context, sbn.packageName, 0)
    }

    private fun supportsIntentBasedCount(context: Context, packageName: String): Boolean {
        return sharedPref.getStringSet(context.getString(R.string.key_badgeIntentPackages), emptySet())!!.contains(packageName)
    }
}
