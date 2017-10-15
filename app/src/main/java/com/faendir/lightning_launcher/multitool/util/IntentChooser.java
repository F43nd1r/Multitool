package com.faendir.lightning_launcher.multitool.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;

import org.acra.ACRA;

public class IntentChooser extends BaseActivity {

    public IntentChooser() {
        super(R.layout.content_intent_chooser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TabHost host = findViewById(R.id.tabHost);
        host.setup();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        load(host, R.id.apps, R.string.title_apps, "a", intent, false);
        load(host, R.id.shortcuts, R.string.title_shortcuts, "s", new Intent(Intent.ACTION_CREATE_SHORTCUT), true);
    }

    private void load(TabHost host, @IdRes int id, @StringRes int title, String tag, Intent intent, boolean isIndirect){
        TabHost.TabSpec tab = host.newTabSpec(tag);
        tab.setContent(id);
        tab.setIndicator(getString(title));
        host.addTab(tab);
        new IntentHandlerListTask(this, intent, isIndirect, id).execute();
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

    boolean handleSelection(IntentInfo info) {
        if (info.isIndirect()) {
            startActivityForResult(info.getIntent(), 0);
        } else if (info.getIntent() != null) {
            setResult(info.getIntent(), info.getName());
            finish();
        } else {
            nullIntent();
            ACRA.getErrorReporter().handleSilentException(new NullPointerException(info.getName() + " intent was null"));
        }
        return true;
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

}
