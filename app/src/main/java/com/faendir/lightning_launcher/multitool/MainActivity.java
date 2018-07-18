package com.faendir.lightning_launcher.multitool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.faendir.lightning_launcher.multitool.billing.BillingManager;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.multitool.util.BaseActivity;
import com.faendir.lightning_launcher.multitool.util.FragmentManager;
import com.faendir.lightning_launcher.multitool.util.Fragments;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import org.greenrobot.eventbus.EventBus;

public class MainActivity extends BaseActivity {
    private FragmentManager fragmentManager;
    private Drawer drawer;
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
        DrawerBuilder builder = new DrawerBuilder(this).withToolbar(getToolbar());
        Fragments.stream().forEach(f -> f.addTo(builder));
        drawer = builder.addDrawerItems(new DividerDrawerItem(),
                new SecondaryDrawerItem().withName(R.string.play_store)
                        .withIdentifier(R.string.play_store)
                        .withSelectable(false)
                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                            } catch (android.content.ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                            }
                            return true;
                        }),
                new SecondaryDrawerItem().withName(R.string.google_community)
                        .withIdentifier(R.string.google_community)
                        .withSelectable(false)
                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_googlePlus))));
                            return true;
                        }),
                new SecondaryDrawerItem().withName(R.string.email)
                        .withIdentifier(R.string.email)
                        .withSelectable(false)
                        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                                    Uri.fromParts(getString(R.string.link_email_scheme), getString(R.string.link_email_adress), null));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.link_email_subject));
                            startActivity(Intent.createChooser(emailIntent, getString(R.string.link_email_chooser)));
                            return true;
                        })).withSelectedItem(-1).withCloseOnClick(true).build();
        fragmentManager = new FragmentManager(this, billingManager, drawer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(fragmentManager);
        if (!fragmentManager.loadLastFragment() && !drawer.isDrawerOpen()) {
            drawer.openDrawer();
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
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        }else {
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
