package com.faendir.lightning_launcher.multitool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.faendir.lightning_launcher.multitool.billing.BillingManager;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.multitool.event.UpdateActionModeRequest;
import com.faendir.lightning_launcher.multitool.util.BaseActivity;
import com.faendir.lightning_launcher.multitool.util.DrawerManager;
import com.faendir.lightning_launcher.multitool.util.FragmentManager;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends BaseActivity {

    private FragmentManager fragmentManager;
    private DrawerManager drawerManager;
    private ActionMode actionMode;
    private BillingManager billingManager;

    public MainActivity() {
        super(R.layout.content_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
        billingManager = new BillingManager(this);
        initDrawer();
    }

    private void initDrawer() {
        Drawer drawer = new DrawerBuilder(this)
                .withToolbar(getToolbar())
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.title_launcherScript).withIdentifier(R.string.title_launcherScript),
                        new PrimaryDrawerItem().withName(R.string.title_scriptManager).withIdentifier(R.string.title_scriptManager),
                        new PrimaryDrawerItem().withName(R.string.title_gestureLauncher).withIdentifier(R.string.title_gestureLauncher),
                        new PrimaryDrawerItem().withName(R.string.title_musicWidget).withIdentifier(R.string.title_musicWidget),
                        new SectionDrawerItem().withName(R.string.title_links).withIsExpanded(true).withSubItems(
                                new SecondaryDrawerItem().withName(R.string.play_store).withIdentifier(R.string.play_store).withSelectable(false),
                                new SecondaryDrawerItem().withName(R.string.google_community).withIdentifier(R.string.google_community).withSelectable(false),
                                new SecondaryDrawerItem().withName(R.string.email).withIdentifier(R.string.email).withSelectable(false)
                        ))
                .addStickyDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.title_settings).withIdentifier(R.string.title_settings).withSelectable(false))
                .withSelectedItem(-1)
                .withCloseOnClick(true)
                .build();
        drawerManager = new DrawerManager(this, drawer);
        fragmentManager = new FragmentManager(this, billingManager, drawer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        EventBus.getDefault().register(fragmentManager);
        if (!fragmentManager.loadLastFragment()) {
            drawerManager.openDrawer();
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(fragmentManager);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (billingManager != null) {
            billingManager.release();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!drawerManager.closeDrawer()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingManager.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onButtonClick(View v) {
        EventBus.getDefault().post(new ClickEvent(v.getId()));
    }

    @Subscribe
    public void onUpdateActionModeRequest(final UpdateActionModeRequest request) {
        if (actionMode == null && request.show()) {
            startSupportActionMode(new ActionMode.Callback() {
                final ActionMode.Callback delegate = request.getCallback();

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    actionMode = mode;
                    return delegate.onCreateActionMode(mode, menu);
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return delegate.onPrepareActionMode(mode, menu);
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return delegate.onActionItemClicked(mode, item);
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    delegate.onDestroyActionMode(mode);
                    actionMode = null;
                }
            });
        } else if (actionMode != null) {
            if (!request.show()) {
                actionMode.finish();
            } else {
                actionMode.invalidate();
            }
        }
    }
}
