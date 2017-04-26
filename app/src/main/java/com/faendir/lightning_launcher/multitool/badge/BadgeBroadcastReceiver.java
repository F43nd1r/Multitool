package com.faendir.lightning_launcher.multitool.badge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author F43nd1r
 * @since 25.04.2017
 */

public class BadgeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(intent);
        service.setClass(context, BadgeService.class);
        context.startService(service);
    }
}
