package com.faendir.lightning_launcher.multitool.badge

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R

/**
 * @author F43nd1r
 * @since 25.04.2017
 */
class BadgeBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null)
            when (action) {
                ASUS_INTENT_BADGE_COUNT_UPDATE -> if (intent.hasExtra(ASUS_BADGE_COUNT) && intent.hasExtra(ASUS_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(ASUS_PACKAGE_NAME), intent.getIntExtra(ASUS_BADGE_COUNT, 0), action)
                }
                ADW_INTENT_BADGE_COUNT_UPDATE -> if (intent.hasExtra(ADW_BADGE_COUNT) && intent.hasExtra(ADW_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(ADW_PACKAGE_NAME), intent.getIntExtra(ADW_BADGE_COUNT, 0), action)
                }
                APEX_INTENT_BADGE_COUNT_UPDATE -> if (intent.hasExtra(APEX_BADGE_COUNT) && intent.hasExtra(APEX_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(APEX_PACKAGE_NAME), intent.getIntExtra(APEX_BADGE_COUNT, 0), action)
                }
                HTC_INTENT_BADGE_COUNT_UPDATE -> if (intent.hasExtra(HTC_BADGE_COUNT) && intent.hasExtra(HTC_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(HTC_PACKAGE_NAME), intent.getIntExtra(HTC_BADGE_COUNT, 0), action)
                }
                HTC_NEW_INTENT_BADGE_COUNT_UPDATE -> if (intent.hasExtra(HTC_NEW_BADGE_COUNT) && intent.hasExtra(HTC_NEW_COMPONENT)) {
                    val componentName = ComponentName.unflattenFromString(intent.getStringExtra(HTC_NEW_COMPONENT))
                    if (componentName != null) {
                        updateBadgeCount(context, componentName.packageName, intent.getIntExtra(HTC_NEW_BADGE_COUNT, 0), action)
                    }
                }
                SONY_INTENT_BADGE_COUNT_UPDATE -> if (intent.hasExtra(SONY_BADGE_COUNT) && intent.hasExtra(SONY_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(SONY_PACKAGE_NAME), Integer.parseInt(intent.getStringExtra(SONY_BADGE_COUNT)), action)
                }
                VIVO_INTENT_BADGE_COUNT_UPDATE -> if (intent.hasExtra(VIVO_BADGE_COUNT) && intent.hasExtra(VIVO_PACKAGE_NAME)) {
                    updateBadgeCount(context, intent.getStringExtra(VIVO_PACKAGE_NAME), intent.getIntExtra(VIVO_BADGE_COUNT, 0), action)
                }
            }
    }

    private fun updateBadgeCount(context: Context, packageName: String, count: Int, action: String) {
        if (MultiTool.DEBUG) Log.d(MultiTool.LOG_TAG, "Notification count for $packageName updated to $count by Intent $action")
        BadgeDataSource.setBadgeCount(context, packageName, count)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val set: MutableSet<String> = HashSet(sharedPref.getStringSet(context.getString(R.string.key_badgeIntentPackages), emptySet())!!)
        if (!set.contains(packageName)) {
            set.add(packageName)
            sharedPref.edit().putStringSet(context.getString(R.string.key_badgeIntentPackages), set).apply()
        }
    }

    companion object {
        private const val ASUS_BADGE_COUNT = "badge_count"
        private const val ASUS_PACKAGE_NAME = "badge_count_package_name"
        private const val ASUS_INTENT_BADGE_COUNT_UPDATE = "android.intent.action.BADGE_COUNT_UPDATE"
        private const val ADW_INTENT_BADGE_COUNT_UPDATE = "org.adw.launcher.counter.SEND"
        private const val ADW_PACKAGE_NAME = "PNAME"
        private const val ADW_BADGE_COUNT = "COUNT"
        private const val APEX_INTENT_BADGE_COUNT_UPDATE = "com.anddoes.launcher.COUNTER_CHANGED"
        private const val APEX_PACKAGE_NAME = "package"
        private const val APEX_BADGE_COUNT = "count"
        private const val HTC_INTENT_BADGE_COUNT_UPDATE = "com.htc.launcher.action.UPDATE_SHORTCUT"
        private const val HTC_PACKAGE_NAME = "packagename"
        private const val HTC_BADGE_COUNT = "count"
        private const val HTC_NEW_INTENT_BADGE_COUNT_UPDATE = "com.htc.launcher.action.SET_NOTIFICATION"
        private const val HTC_NEW_COMPONENT = "com.htc.launcher.extra.COMPONENT"
        private const val HTC_NEW_BADGE_COUNT = "com.htc.launcher.extra.COUNT"
        private const val SONY_INTENT_BADGE_COUNT_UPDATE = "com.sonyericsson.home.action.UPDATE_BADGE"
        private const val SONY_PACKAGE_NAME = "com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME"
        private const val SONY_BADGE_COUNT = "com.sonyericsson.home.intent.extra.badge.MESSAGE"
        private const val VIVO_INTENT_BADGE_COUNT_UPDATE = "launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM"
        private const val VIVO_PACKAGE_NAME = "packageName"
        private const val VIVO_BADGE_COUNT = "notificationNum"
    }
}
