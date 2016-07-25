package com.faendir.lightning_launcher.multitool;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.multitool.event.LeaveApplicationRequest;
import com.faendir.lightning_launcher.multitool.event.UpdateActionModeRequest;
import com.faendir.lightning_launcher.multitool.util.BaseActivity;
import com.faendir.lightning_launcher.multitool.util.DrawerManager;
import com.faendir.lightning_launcher.multitool.util.FragmentManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends BaseActivity {

    private FragmentManager fragmentManager;
    private DrawerManager drawerManager;
    private ActionMode actionMode;

    public MainActivity() {
        super(R.layout.content_main, R.menu.drawer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
        fragmentManager = new FragmentManager(this);
    }

    @Override
    protected void initNavView(NavigationView navigationView) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            navigationView.getMenu().removeItem(R.id.nav_music);
        }
        drawerManager = new DrawerManager(this, getLayout());
        navigationView.setNavigationItemSelectedListener(drawerManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        EventBus.getDefault().register(fragmentManager);
        if(!fragmentManager.loadLastFragment()){
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
    public void onBackPressed() {
        if (!drawerManager.closeDrawer()) {
            super.onBackPressed();
        }
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
