package com.faendir.lightning_launcher.multitool.util;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.faendir.lightning_launcher.multitool.MainActivity;
import com.faendir.lightning_launcher.multitool.settings.PrefsFragment;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest;
import com.faendir.lightning_launcher.multitool.gesture.GestureFragment;
import com.faendir.lightning_launcher.multitool.launcherscript.LauncherScriptFragment;
import com.faendir.lightning_launcher.multitool.music.MusicFragment;
import com.faendir.lightning_launcher.multitool.scriptmanager.ScriptManagerFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
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
            case R.id.nav_music:
                currentFragment = new MusicFragment();
                break;
            case R.id.nav_settings:
                currentFragment = new PrefsFragment();
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

    public boolean loadLastFragment() {
        if (sharedPref.contains(context.getString(R.string.pref_lastFragment))) {
            int load = context.getResources().getIdentifier(sharedPref.getString(context.getString(R.string.pref_lastFragment), null), "id", context.getPackageName());
            NavigationView navView = context.getNavigationView();
            MenuItem item = navView.getMenu().findItem(load);
            String title = item != null ? item.getTitle().toString() : context.getString(R.string.title_launcherScript);
            EventBus.getDefault().post(new SwitchFragmentRequest(load, title));
            return true;
        }
        return false;
    }
}
