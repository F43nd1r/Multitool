package com.faendir.lightning_launcher.multitool.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import java8.util.function.Consumer;

/**
 * Created on 12.07.2016.
 *
 * @author F43nd1r
 */

class IntentHandlerListTask extends AsyncTask<Void, Void, List<IntentInfo>> {

    private final PackageManager pm;
    private final Intent intent;
    private final boolean isIndirect;
    private final Consumer<List<IntentInfo>> postExecute;

    IntentHandlerListTask(Context context, Intent intent, boolean isIndirect, Consumer<List<IntentInfo>> postExecute) {
        this.postExecute = postExecute;
        this.pm = context.getPackageManager();
        this.intent = intent;
        this.isIndirect = isIndirect;
    }

    @Override
    protected final List<IntentInfo> doInBackground(Void... params) {
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        List<IntentInfo> infos = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveInfos) {
            ActivityInfo activity = resolveInfo.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                    activity.name);
            Intent launchIntent = new Intent(intent);
            launchIntent.setComponent(name);
            IntentInfo intentInfo = new IntentInfo(launchIntent, new ResolveInfoDrawableProvider(pm, resolveInfo), resolveInfo.loadLabel(pm).toString(), isIndirect);
            boolean found = false;
            for (IntentInfo info : infos) {
                if (info.getIntent().getComponent().getPackageName().equals(intentInfo.getIntent().getComponent().getPackageName())
                        && info.getName().equals(intentInfo.getName())) {
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

    @Override
    protected void onPostExecute(List<IntentInfo> infos) {
        postExecute.accept(infos);
    }
}
