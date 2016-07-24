package com.faendir.lightning_launcher.multitool.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;

import org.acra.ACRA;

import java.util.List;

public class IntentChooser extends BaseActivity implements AdapterView.OnItemClickListener {

    public IntentChooser() {
        super(R.layout.content_intent_chooser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TabHost host = (TabHost) findViewById(R.id.tabHost);
        Intent i = getIntent();
        boolean enableShortcuts = i.getBooleanExtra(getString(R.string.key_shortcuts), false);
        boolean useAppInfo = i.getBooleanExtra(getString(R.string.key_appInfo), false);
        Intent intent = i.getParcelableExtra(Intent.EXTRA_INTENT);
        IntentTarget target = (IntentTarget) i.getSerializableExtra(getString(R.string.key_target));
        if (enableShortcuts) {
            host.setup();
            TabHost.TabSpec apps = host.newTabSpec("Apps");
            apps.setContent(R.id.apps);
            apps.setIndicator("Apps");
            host.addTab(apps);
            TabHost.TabSpec shortcuts = host.newTabSpec("Shortcuts");
            shortcuts.setContent(R.id.shortcuts);
            shortcuts.setIndicator("Shortcuts");
            host.addTab(shortcuts);
            loadShortcuts();
        } else {
            ViewGroup parent = (ViewGroup) host.getParent();
            parent.removeView(host);
            View apps = host.findViewById(R.id.apps);
            ((ViewGroup) apps.getParent()).removeView(apps);
            parent.addView(apps);
        }
        loadApps(intent, target, useAppInfo);
    }

    private void loadApps(Intent intent, IntentTarget target, boolean useApplication) {
        new BaseIntentHandlerListTask(getPackageManager(), intent, false, target, useApplication) {
            @Override
            protected void onPostExecute(List<IntentInfo> infos) {
                View root = findViewById(R.id.apps);
                ListView listView = (ListView) root.findViewById(R.id.list);
                listView.setAdapter(new ListAdapter<>(IntentChooser.this, infos));
                listView.setVisibility(View.VISIBLE);
                listView.setOnItemClickListener(IntentChooser.this);
                root.findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        }.execute();
    }

    private void loadShortcuts() {
        new BaseIntentHandlerListTask(getPackageManager(), new Intent(Intent.ACTION_CREATE_SHORTCUT), true, IntentTarget.ACTIVITY, false) {
            @Override
            protected void onPostExecute(List<IntentInfo> infos) {
                View root = findViewById(R.id.shortcuts);
                ListView listView = (ListView) root.findViewById(R.id.list);
                assert listView != null;
                listView.setAdapter(new ListAdapter<>(IntentChooser.this, infos));
                listView.setVisibility(View.VISIBLE);
                listView.setOnItemClickListener(IntentChooser.this);
                root.findViewById(R.id.progressBar).setVisibility(View.GONE);
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
        } else if (info.getIntent() != null) {
            setResult(info.getIntent(), info.getText());
        } else {
            nullIntent();
            ACRA.getErrorReporter().handleSilentException(new NullPointerException(info.getText() + " intent was null"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
            if (intent != null) {
                setResult(intent, data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME));
            } else {
                nullIntent();
                ACRA.getErrorReporter().handleSilentException(new NullPointerException("Shortcut intent was null"));
            }
        }
    }

    private void setResult(Intent intent, String label) {
        Intent result = new Intent();
        result.putExtra(Intent.EXTRA_INTENT, intent);
        result.putExtra(Intent.EXTRA_TITLE, label);
        setResult(RESULT_OK, result);
        finish();
    }

    private void nullIntent() {
        Toast.makeText(this, "Failed to load action info", Toast.LENGTH_SHORT).show();
    }

    public enum IntentTarget {
        ACTIVITY,
        BROADCAST_RECEIVER
    }

    public static class Builder {
        private final Activity context;
        private final Intent intent;

        public Builder(Activity context) {
            this.context = context;
            intent = new Intent(context, IntentChooser.class);
            setDefaults();
        }

        void setDefaults() {
            intent.putExtra(context.getString(R.string.key_shortcuts), false);
            intent.putExtra(context.getString(R.string.key_appInfo), false);
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.putExtra(Intent.EXTRA_INTENT, i);
            intent.putExtra(context.getString(R.string.key_target), IntentTarget.ACTIVITY);
        }

        public Builder enableShortcuts() {
            intent.putExtra(context.getString(R.string.key_shortcuts), true);
            return this;
        }

        public Builder useIntent(Intent i, IntentTarget target) {
            intent.putExtra(Intent.EXTRA_INTENT, i);
            intent.putExtra(context.getString(R.string.key_target), target);
            return this;
        }

        public Builder useApplicationInfo() {
            intent.putExtra(context.getString(R.string.key_appInfo), true);
            return this;
        }

        public void startForResult(int requestCode) {
            context.startActivityForResult(intent, requestCode);
        }
    }
}
