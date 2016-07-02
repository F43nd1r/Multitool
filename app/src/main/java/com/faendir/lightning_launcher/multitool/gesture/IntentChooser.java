package com.faendir.lightning_launcher.multitool.gesture;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.ListAdapter;

import org.acra.ACRA;

import java.util.List;

public class IntentChooser extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_chooser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TabHost host = (TabHost) findViewById(R.id.tabHost);
        host.setup();
        TabHost.TabSpec apps = host.newTabSpec("Apps");
        apps.setContent(R.id.apps);
        apps.setIndicator("Apps");
        host.addTab(apps);
        TabHost.TabSpec shortcuts = host.newTabSpec("Shortcuts");
        shortcuts.setContent(R.id.shortcuts);
        shortcuts.setIndicator("Shortcuts");
        host.addTab(shortcuts);
        loadApps();
        loadShortcuts();
    }

    private void loadApps() {
        new AsyncTask<Void, Void, IntentInfo[]>() {
            @Override
            protected IntentInfo[] doInBackground(Void... params) {
                PackageManager pm = getPackageManager();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
                IntentInfo[] infos = new IntentInfo[resolveInfos.size()];
                for (int i = 0; i < infos.length; i++) {
                    ResolveInfo info = resolveInfos.get(i);
                    ActivityInfo activity = info.activityInfo;
                    ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                            activity.name);
                    Intent launchIntent = new Intent(Intent.ACTION_MAIN);
                    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    launchIntent.setComponent(name);
                    infos[i] = new IntentInfo(launchIntent, new ResolveInfoDrawableProvider(pm, info), info.loadLabel(pm).toString(), false);
                }
                return infos;
            }

            @Override
            protected void onPostExecute(IntentInfo[] infos) {
                ListView listView = (ListView) findViewById(R.id.apps_list);
                listView.setAdapter(new ListAdapter<>(IntentChooser.this, infos));
                listView.setVisibility(View.VISIBLE);
                listView.setOnItemClickListener(IntentChooser.this);
                findViewById(R.id.apps_progressBar).setVisibility(View.GONE);
            }
        }.execute();
    }

    private void loadShortcuts() {
        new AsyncTask<Void, Void, IntentInfo[]>() {
            @Override
            protected IntentInfo[] doInBackground(Void... params) {
                PackageManager pm = getPackageManager();
                Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
                IntentInfo[] infos = new IntentInfo[resolveInfos.size()];
                for (int i = 0; i < infos.length; i++) {
                    ResolveInfo info = resolveInfos.get(i);
                    ActivityInfo activity = info.activityInfo;
                    ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                            activity.name);
                    Intent launchIntent = new Intent(Intent.ACTION_MAIN);
                    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    launchIntent.setComponent(name);
                    infos[i] = new IntentInfo(launchIntent, new ResolveInfoDrawableProvider(pm, info), info.loadLabel(pm).toString(), true);
                }
                return infos;
            }

            @Override
            protected void onPostExecute(IntentInfo[] infos) {
                ListView listView = (ListView) findViewById(R.id.shortcuts_list);
                assert listView != null;
                listView.setAdapter(new ListAdapter<>(IntentChooser.this, infos));
                listView.setVisibility(View.VISIBLE);
                listView.setOnItemClickListener(IntentChooser.this);
                findViewById(R.id.shortcuts_progressBar).setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        handleSelection((IntentInfo) parent.getAdapter().getItem(position));
    }

    private void handleSelection(IntentInfo info) {
        if (info.isIndirect()) {
            startActivityForResult(info.getIntent(), 0);
        } else if(info.getIntent() != null){
            setResult(RESULT_OK, info.getIntent());
            finish();
        } else {
            nullIntent();
            ACRA.getErrorReporter().handleSilentException(new NullPointerException(info.getText()+" intent was null"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
            if(intent != null) {
                setResult(RESULT_OK, intent);
                finish();
            }else {
                nullIntent();
                ACRA.getErrorReporter().handleSilentException(new NullPointerException("Shortcut intent was null"));
            }
        }
    }

    private void nullIntent(){
        Toast.makeText(this, "Failed to load action info", Toast.LENGTH_SHORT).show();
    }
}
