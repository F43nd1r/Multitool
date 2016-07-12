package com.faendir.lightning_launcher.multitool.util;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lukas on 12.07.2016.
 */

public abstract class BaseIntentHandlerListTask extends AsyncTask<Void, Void, List<IntentInfo>> {

    private final PackageManager pm;
    private final Intent intent;
    private final boolean isIndirect;
    private final boolean loadReceiver;

    public BaseIntentHandlerListTask(PackageManager packageManager, Intent intent, boolean isIndirect, boolean loadReceiver) {
        this.pm = packageManager;
        this.intent = intent;
        this.isIndirect = isIndirect;
        this.loadReceiver = loadReceiver;
    }

    @Override
    protected final List<IntentInfo> doInBackground(Void... params) {
        List<ResolveInfo> resolveInfos = loadReceiver ? pm.queryBroadcastReceivers(intent, 0) : pm.queryIntentActivities(intent, 0);
        List<IntentInfo> infos = new ArrayList<>();
        for (int i = 0; i < resolveInfos.size(); i++) {
            ResolveInfo info = resolveInfos.get(i);
            ActivityInfo activity = info.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                    activity.name);
            Intent launchIntent = new Intent(intent);
            launchIntent.setComponent(name);
            IntentInfo intentInfo = new IntentInfo(launchIntent, new ResolveInfoDrawableProvider(pm, info), info.loadLabel(pm).toString(), isIndirect);
            boolean found = false;
            for (IntentInfo x : infos) {
                if (x.getIntent().getComponent().getPackageName().equals(intentInfo.getIntent().getComponent().getPackageName())
                        && x.getText().equals(intentInfo.getText())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                infos.add(intentInfo);
            }
        }
        return infos;
    }
}
