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
 * Created on 12.07.2016.
 *
 * @author F43nd1r
 */

abstract class BaseIntentHandlerListTask extends AsyncTask<Void, Void, List<IntentInfo>> {

    private final PackageManager pm;
    private final Intent intent;
    private final boolean isIndirect;
    private final IntentChooser.IntentTarget target;
    private final boolean useApplicationLabel;

    public BaseIntentHandlerListTask(PackageManager packageManager, Intent intent, boolean isIndirect, IntentChooser.IntentTarget target, boolean useApplicationLabel) {
        this.pm = packageManager;
        this.intent = intent;
        this.isIndirect = isIndirect;
        this.target = target;
        this.useApplicationLabel = useApplicationLabel;
    }

    @Override
    protected final List<IntentInfo> doInBackground(Void... params) {
        List<ResolveInfo> resolveInfos = queryInfos();
        List<IntentInfo> infos = new ArrayList<>();
        for (int i = 0; i < resolveInfos.size(); i++) {
            ResolveInfo info = resolveInfos.get(i);
            ActivityInfo activity = info.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                    activity.name);
            Intent launchIntent = new Intent(intent);
            launchIntent.setComponent(name);
            IntentInfo intentInfo = new IntentInfo(launchIntent, new ResolveInfoDrawableProvider(pm, info), getLabel(info), isIndirect);
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

    private List<ResolveInfo> queryInfos() {
        switch (target) {
            case ACTIVITY:
                return pm.queryIntentActivities(intent, 0);
            case BROADCAST_RECEIVER:
                return pm.queryBroadcastReceivers(intent, 0);
            default:
                return new ArrayList<>();
        }
    }

    private String getLabel(ResolveInfo info) {
        return (useApplicationLabel ? pm.getApplicationLabel(info.activityInfo.applicationInfo) : info.loadLabel(pm)).toString();
    }
}
