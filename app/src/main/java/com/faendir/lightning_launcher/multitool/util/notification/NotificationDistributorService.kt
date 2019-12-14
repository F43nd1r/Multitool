package com.faendir.lightning_launcher.multitool.util.notification

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.badge.BadgeNotificationListener
import com.faendir.lightning_launcher.multitool.music.MusicNotificationListener
import com.faendir.lightning_launcher.scriptlib.DialogActivity

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class NotificationDistributorService : NotificationListenerService() {
    private val listeners: List<NotificationListener> = listOf(BadgeNotificationListener(), MusicNotificationListener())

    override fun onCreate() {
        super.onCreate()
        listeners.forEach { it.onCreate(this) }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) = listeners.forEach { l -> l.onNotificationPosted(this, sbn) }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) = onNotificationPosted(sbn)

    override fun onNotificationRemoved(sbn: StatusBarNotification) = listeners.forEach { l -> l.onNotificationRemoved(this, sbn) }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap) = onNotificationRemoved(sbn)

    companion object {

        fun isDisabled(context: Context): Boolean {
            val notificationListener = ComponentName(context, NotificationDistributorService::class.java)
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return flat == null || !flat.contains(notificationListener.flattenToString())
        }

        fun askForEnable(context: Context) {
            DialogActivity.Builder(context, R.style.AppTheme_Dialog_Alert)
                    .setTitle(R.string.title_listener)
                    .setMessage(R.string.text_listener)
                    .setButtons(android.R.string.yes, android.R.string.no, object : ResultReceiver(Handler()) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            super.onReceiveResult(resultCode, resultData)
                            if (resultCode == AlertDialog.BUTTON_POSITIVE) {
                                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        }
                    }).show()
        }
    }
}
