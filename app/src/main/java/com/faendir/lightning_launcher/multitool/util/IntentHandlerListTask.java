package com.faendir.lightning_launcher.multitool.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.omniadapter.OmniBuilder;
import com.faendir.omniadapter.model.Action;
import com.faendir.omniadapter.model.DeepObservableList;

import org.apache.commons.collections4.comparators.ComparableComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 12.07.2016.
 *
 * @author F43nd1r
 */

class IntentHandlerListTask extends AsyncTask<Void, Void, List<IntentInfo>> {

    private final IntentChooser context;
    private final PackageManager pm;
    private final Intent intent;
    private final boolean isIndirect;
    private final IntentChooser.IntentTarget target;
    private final boolean useApplicationLabel;
    private final int rootId;

    public IntentHandlerListTask(IntentChooser context, Intent intent, boolean isIndirect,
                                 IntentChooser.IntentTarget target, boolean useApplicationLabel, @IdRes int rootId) {
        this.context = context;
        this.pm = context.getPackageManager();
        this.intent = intent;
        this.isIndirect = isIndirect;
        this.target = target;
        this.useApplicationLabel = useApplicationLabel;
        this.rootId = rootId;
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

    @Override
    protected void onPostExecute(List<IntentInfo> infos) {
        View root = context.findViewById(rootId);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.list);
        DeepObservableList<IntentInfo> list = DeepObservableList.copyOf(IntentInfo.class, infos);
        list.beginBatchedUpdates();
        list.keepSorted(new ComparableComparator<IntentInfo>());
        list.endBatchedUpdates();
        new OmniBuilder<>(context, list, context)
                .setClick(new Action.Click(Action.CUSTOM, context))
                .attach(recyclerView);
        recyclerView.setVisibility(View.VISIBLE);
        root.findViewById(R.id.progressBar).setVisibility(View.GONE);
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
