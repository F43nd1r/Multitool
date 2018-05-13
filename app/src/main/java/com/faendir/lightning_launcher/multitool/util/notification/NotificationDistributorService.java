package com.faendir.lightning_launcher.multitool.util.notification;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.badge.BadgeNotificationListener;
import com.faendir.lightning_launcher.multitool.billing.BaseBillingManager;
import com.faendir.lightning_launcher.multitool.music.MusicNotificationListener;
import com.faendir.lightning_launcher.scriptlib.DialogActivity;

import java.util.ArrayList;
import java.util.List;

import java8.util.stream.StreamSupport;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationDistributorService extends NotificationListenerService {
    private List<NotificationListener> listeners;

    public NotificationDistributorService() {
        listeners = new ArrayList<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final BadgeNotificationListener badgeNotificationListener = new BadgeNotificationListener();
        badgeNotificationListener.onCreate(this);
        listeners.add(badgeNotificationListener);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            new Thread(() -> {
                if(new BaseBillingManager(this).isBoughtOrTrial(R.string.title_musicWidget)){
                    final MusicNotificationListener musicNotificationListener = new MusicNotificationListener();
                    musicNotificationListener.onCreate(this);
                    listeners.add(musicNotificationListener);
                }
            }).start();
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        StreamSupport.stream(listeners).forEach(l -> l.onNotificationPosted(this, sbn));
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        StreamSupport.stream(listeners).forEach(l -> l.onNotificationRemoved(this, sbn));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        onNotificationRemoved(sbn);
    }

    public static boolean isEnabled(@NonNull Context context) {
        ComponentName notificationListener = new ComponentName(context, NotificationDistributorService.class);
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
