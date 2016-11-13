package com.faendir.lightning_launcher.multitool.util;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.omniadapter.OmniAdapter;
import com.faendir.omniadapter.model.Action;
import com.faendir.omniadapter.model.Component;
import com.faendir.omniadapter.model.DeepObservableList;

import org.acra.ACRA;

public class IntentChooser extends BaseActivity implements OmniAdapter.Controller<IntentInfo>, Action.Click.Listener {

    public IntentChooser() {
        super(R.layout.content_intent_chooser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TabHost host = (TabHost) findViewById(R.id.tabHost);
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

}
