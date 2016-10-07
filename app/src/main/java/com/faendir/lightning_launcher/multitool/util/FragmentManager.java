package com.faendir.lightning_launcher.multitool.util;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;

import com.faendir.lightning_launcher.multitool.MainActivity;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.billing.BillingManager;
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest;
import com.faendir.lightning_launcher.multitool.gesture.GestureFragment;
import com.faendir.lightning_launcher.multitool.launcherscript.LauncherScriptFragment;
import com.faendir.lightning_launcher.multitool.music.MusicFragment;
import com.faendir.lightning_launcher.multitool.scriptmanager.ScriptManagerFragment;
import com.faendir.lightning_launcher.multitool.settings.PrefsFragment;
import com.mikepenz.materialdrawer.Drawer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
public class FragmentManager {
    private final MainActivity context;
    private final android.app.FragmentManager manager;
    private final SharedPreferences sharedPref;
    private final BillingManager billingManager;
    private final Drawer drawer;
    private Fragment currentFragment;

    public FragmentManager(MainActivity context, BillingManager billingManager, Drawer drawer) {
        this.context = context;
        this.manager = context.getFragmentManager();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.billingManager = billingManager;
        this.drawer = drawer;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSwitchFragmentRequest(final SwitchFragmentRequest request) {
        String name;
        try {
            name = context.getResources().getResourceName(request.getId());
        }catch (Resources.NotFoundException e){
            name = "none";
        }
        if (currentFragment != null && sharedPref.getString(context.getString(R.string.pref_lastFragment), "").equals(name)) {
            return;
        }
        if (!billingManager.isBoughtOrTrial(request.getId())) {
            billingManager.showDialog();
        }
        final String finalName = name;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (request.getId()) {
                    case R.string.title_scriptManager:
                        currentFragment = new ScriptManagerFragment();
                        break;
                    case R.string.title_launcherScript:
                        currentFragment = new LauncherScriptFragment();
                        break;
                    case R.string.title_gestureLauncher:
                        currentFragment = new GestureFragment();
                        break;
                    case R.string.title_musicWidget:
                        currentFragment = new MusicFragment();
                        break;
                    case R.string.title_settings:
                        currentFragment = new PrefsFragment();
                        break;
                    default:
                        manager.beginTransaction().remove(currentFragment).commit();
                        currentFragment = null;
                        drawer.openDrawer();
                }
                if (currentFragment != null) {
                    manager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();
                    sharedPref.edit().putString(context.getString(R.string.pref_lastFragment), finalName).apply();
                    ActionBar toolbar = context.getSupportActionBar();
                    if (toolbar != null) {
                        toolbar.setTitle(context.getString(request.getId()));
                    }
                    drawer.setSelection(request.getId());
                }
            }
        });
    }

    public boolean loadLastFragment() {
        if (sharedPref.contains(context.getString(R.string.pref_lastFragment))) {
            int load = context.getResources().getIdentifier(sharedPref.getString(context.getString(R.string.pref_lastFragment), null), "id", context.getPackageName());
            EventBus.getDefault().post(new SwitchFragmentRequest(load));
            return true;
        }
        return false;
    }
}
