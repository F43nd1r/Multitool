package com.faendir.lightning_launcher.multitool.music;

import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.faendir.lightning_launcher.multitool.MultiTool;

/**
 * Created on 03.07.2016.
 *
 * @author F43nd1r
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DummyNotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(MultiTool.DEBUG) Log.d(MultiTool.LOG_TAG, sbn.toString());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if(MultiTool.DEBUG) Log.d(MultiTool.LOG_TAG, sbn.toString());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap) {
        onNotificationRemoved(sbn);
    }
}
