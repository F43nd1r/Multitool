package com.faendir.lightning_launcher.multitool.util;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem;
import com.mikepenz.fastadapter.commons.adapters.GenericFastItemAdapter;

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
    private final int rootId;

    IntentHandlerListTask(IntentChooser context, Intent intent, boolean isIndirect, @IdRes int rootId) {
        this.context = context;
        this.pm = context.getPackageManager();
        this.intent = intent;
        this.isIndirect = isIndirect;
        this.rootId = rootId;
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
        View root = context.findViewById(rootId);
        RecyclerView recyclerView = root.findViewById(R.id.list);
        GenericFastItemAdapter<IntentInfo, ExpandableItem<IntentInfo>> adapter = new GenericFastItemAdapter<>(ExpandableItem::new);
        adapter.getGenericItemAdapter().withComparator((o1, o2) -> o1.getModel().compareTo(o2.getModel()));
        adapter.withOnClickListener((v, adapter1, item, position) -> context.handleSelection(item.getModel()));
        adapter.setModel(infos);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        root.findViewById(R.id.progressBar).setVisibility(View.GONE);
    }
}
