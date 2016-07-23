package com.faendir.lightning_launcher.multitool.backup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created on 23.07.2016.
 *
 * @author F43nd1r
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        BackupUtils.scheduleNext(context);
    }
}
