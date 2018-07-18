package com.faendir.lightning_launcher.multitool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.faendir.lightning_launcher.multitool.billing.BillingManager;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.multitool.util.BaseActivity;
import com.faendir.lightning_launcher.multitool.util.DrawerManager;
import com.faendir.lightning_launcher.multitool.util.FragmentManager;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import org.greenrobot.eventbus.EventBus;

public class MainActivity extends BaseActivity {
    private FragmentManager fragmentManager;
    private DrawerManager drawerManager;
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
        Drawer drawer = new DrawerBuilder(this).withToolbar(getToolbar())
                .addDrawerItems(new PrimaryDrawerItem().withName(R.string.title_launcherScript).withIdentifier(R.string.title_launcherScript),
                        new PrimaryDrawerItem().withName(R.string.title_scriptManager).withIdentifier(R.string.title_scriptManager),
                        new PrimaryDrawerItem().withName(R.string.title_gestureLauncher).withIdentifier(R.string.title_gestureLauncher),
                        new PrimaryDrawerItem().withName(R.string.title_musicWidget).withIdentifier(R.string.title_musicWidget),
                        new PrimaryDrawerItem().withName(R.string.title_drawer).withIdentifier(R.string.title_drawer),
                        new PrimaryDrawerItem().withName(R.string.title_backup).withIdentifier(R.string.title_backup),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.play_store).withIdentifier(R.string.play_store).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.google_community).withIdentifier(R.string.google_community).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.email).withIdentifier(R.string.email).withSelectable(false))
                .addStickyDrawerItems(new PrimaryDrawerItem().withName(R.string.title_settings).withIdentifier(R.string.title_settings))
                .withSelectedItem(-1)
                .withCloseOnClick(true)
                .build();
        drawerManager = new DrawerManager(this, drawer);
        fragmentManager = new FragmentManager(this, billingManager, drawer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(fragmentManager);
        if (!fragmentManager.loadLastFragment()) {
            drawerManager.openDrawer();
        }
    }

    @Override
    protected void onStop() {
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void onButtonClick(View v) {
        EventBus.getDefault().post(new ClickEvent(v.getId()));
    }
}
