package com.faendir.lightning_launcher.multitool.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.omniadapter.OmniAdapter;
import com.faendir.omniadapter.OmniBuilder;
import com.faendir.omniadapter.model.Action;
import com.faendir.omniadapter.model.Component;
import com.faendir.omniadapter.model.DeepObservableList;

import org.acra.ACRA;

import java.util.List;

public class IntentChooser extends BaseActivity implements OmniAdapter.Controller<IntentInfo>, Action.Click.Listener {

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
            TabHost.TabSpec apps = host.newTabSpec("a");
            apps.setContent(R.id.apps);
            apps.setIndicator(getString(R.string.title_apps));
            host.addTab(apps);
            TabHost.TabSpec shortcuts = host.newTabSpec("s");
            shortcuts.setContent(R.id.shortcuts);
            shortcuts.setIndicator(getString(R.string.title_shortcuts));
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
                RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.list);
                new OmniBuilder<>(IntentChooser.this, DeepObservableList.copyOf(IntentInfo.class, infos), IntentChooser.this)
                        .setClick(new Action.Click(Action.CUSTOM, IntentChooser.this))
                        .attach(recyclerView);
                recyclerView.setVisibility(View.VISIBLE);
                root.findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        }.execute();
    }

    private void loadShortcuts() {
        new BaseIntentHandlerListTask(getPackageManager(), new Intent(Intent.ACTION_CREATE_SHORTCUT), true, IntentTarget.ACTIVITY, false) {
            @Override
            protected void onPostExecute(List<IntentInfo> infos) {
                View root = findViewById(R.id.shortcuts);
                RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.list);
                new OmniBuilder<>(IntentChooser.this, DeepObservableList.copyOf(IntentInfo.class, infos), IntentChooser.this)
                        .setClick(new Action.Click(Action.CUSTOM, IntentChooser.this))
                        .attach(recyclerView);
                recyclerView.setVisibility(View.VISIBLE);
                root.findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSelection(IntentInfo info) {
        if (info.isIndirect()) {
            startActivityForResult(info.getIntent(), 0);
        } else if (info.getIntent() != null) {
            setResult(info.getIntent(), info.getText());
            finish();
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
                finish();
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
        Toast.makeText(this, R.string.toast_cantLoadAction, Toast.LENGTH_SHORT).show();
    }

    @Override
    public View createView(ViewGroup viewGroup, int i) {
        return LayoutInflater.from(this).inflate(R.layout.list_item_app, viewGroup, false);
    }

    @Override
    public void bindView(View view, final IntentInfo intentInfo, int i) {
        final TextView txt = (TextView) view;
        txt.setText(intentInfo.getText());
        int size = (int) getResources().getDimension(android.R.dimen.app_icon_size);
        Drawable img = intentInfo.getImage();
        img.setBounds(0, 0, size, size);
        txt.setCompoundDrawables(img, null, null, null);
    }

    @Override
    public boolean shouldMove(IntentInfo intentInfo, DeepObservableList deepObservableList, int i, DeepObservableList deepObservableList1, int i1) {
        return false;
    }

    @Override
    public boolean isSelectable(IntentInfo intentInfo) {
        return false;
    }

    @Override
    public boolean shouldSwipe(IntentInfo intentInfo, int i) {
        return false;
    }

    @Override
    public boolean allowClick(Component component, int i) {
        return true;
    }

    @Override
    public void onClick(Component component, int i) {
        handleSelection((IntentInfo) component);
    }

    public enum IntentTarget {
        ACTIVITY,
        BROADCAST_RECEIVER
    }

    public static class Builder {
        private final Activity context;
        private final Fragment fragment;
        private final Intent intent;

        public Builder(Activity context) {
            this(context, null);
        }

        public Builder(Fragment fragment) {
            this(fragment.getActivity(), fragment);
        }

        private Builder(Activity context, Fragment fragment) {
            this.context = context;
            this.fragment = fragment;
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
            if (fragment != null) {
                fragment.startActivityForResult(intent, requestCode);
            } else {
                context.startActivityForResult(intent, requestCode);
            }
        }
    }
}
