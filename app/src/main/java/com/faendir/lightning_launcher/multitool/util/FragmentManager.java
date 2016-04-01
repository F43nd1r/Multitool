package com.faendir.lightning_launcher.multitool.util;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.faendir.lightning_launcher.multitool.MainActivity;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest;
import com.faendir.lightning_launcher.multitool.gesture.GestureFragment;
import com.faendir.lightning_launcher.multitool.launcherscript.LauncherScriptFragment;
import com.faendir.lightning_launcher.multitool.scriptmanager.ScriptManagerFragment;
import com.faendir.lightning_launcher.multitool.viewcreator.ViewCreatorFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Lukas on 01.04.2016.
 */
public class FragmentManager {
    private final MainActivity context;
    private final android.app.FragmentManager manager;
    private final SharedPreferences sharedPref;
    private Fragment currentFragment;

    public FragmentManager(MainActivity context) {
        this.context = context;
        this.manager = context.getFragmentManager();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Subscribe
    public void onSwitchFragmentRequest(SwitchFragmentRequest request) {
        String name = context.getResources().getResourceName(request.getId());
        if (currentFragment != null && sharedPref.getString(context.getString(R.string.pref_lastFragment), "").equals(name)) {
            return;
        }
        switch (request.getId()) {
            case R.id.nav_script_manager:
                currentFragment = new ScriptManagerFragment();
                break;
            case R.id.nav_launcher_script:
                currentFragment = new LauncherScriptFragment();
                break;
            case R.id.nav_gesture:
                currentFragment = new GestureFragment();
                break;
            case R.id.nav_view_creator:
                currentFragment = new ViewCreatorFragment();
                break;
            default:
                throw new IllegalArgumentException("Illegal id " + request.getId());
        }
        manager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();
        sharedPref.edit().putString(context.getString(R.string.pref_lastFragment), name).apply();
        ActionBar toolbar = context.getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(request.getTitle());
        }

    }

    public void loadLastFragment() {
        int load = R.id.nav_launcher_script;
        if (sharedPref.contains(context.getString(R.string.pref_lastFragment))) {
            load = context.getResources().getIdentifier(sharedPref.getString(context.getString(R.string.pref_lastFragment), null), "id", context.getPackageName());
        }
        NavigationView navView = context.getNavigationView();
        MenuItem item = navView.getMenu().findItem(load);
        String title = item != null ? item.getTitle().toString() : context.getString(R.string.title_launcherScript);
        EventBus.getDefault().post(new SwitchFragmentRequest(load, title));
    }
}
