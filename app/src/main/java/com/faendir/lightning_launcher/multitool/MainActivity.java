package com.faendir.lightning_launcher.multitool;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.multitool.event.IntentEvent;
import com.faendir.lightning_launcher.multitool.event.LeaveApplicationRequest;
import com.faendir.lightning_launcher.multitool.event.UpdateActionModeRequest;
import com.faendir.lightning_launcher.multitool.util.DrawerManager;
import com.faendir.lightning_launcher.multitool.util.FragmentManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private DrawerManager drawerManager;
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onNewIntent(getIntent());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            navigationView.getMenu().removeItem(R.id.nav_music);
        }
        drawerManager = new DrawerManager(this, drawer);
        navigationView.setNavigationItemSelectedListener(drawerManager);
        fragmentManager = new FragmentManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        EventBus.getDefault().register(fragmentManager);
        fragmentManager.loadLastFragment();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(fragmentManager);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (!drawerManager.closeDrawer()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        EventBus.getDefault().postSticky(new IntentEvent(intent));
    }

    public void onButtonClick(View v) {
        EventBus.getDefault().post(new ClickEvent(v.getId()));
    }

    public NavigationView getNavigationView(){
        return (NavigationView) findViewById(R.id.nav_view);
    }

    @Subscribe
    public void onLeaveRequest(LeaveApplicationRequest request) {
        finish();
    }

    @Subscribe
    public void onUpdateActionModeRequest(final UpdateActionModeRequest request) {
        if(actionMode == null && request.show()) {
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
        } else if(actionMode != null) {
            if(!request.show()){
                actionMode.finish();
            } else {
                actionMode.invalidate();
            }
        }
    }
}
