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
    public static final String EXTRA_MODE = "mode";
    private final MainActivity context;
    private final android.app.FragmentManager manager;
    private final SharedPreferences sharedPref;
    private final BillingManager billingManager;
    private final Drawer drawer;
    private Fragment currentFragment;
    private int lastId;

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
        } catch (Resources.NotFoundException e) {
            name = "none";
        }
        if (currentFragment != null && sharedPref.getString(context.getString(R.string.pref_lastFragment), "").equals(name)) {
            return;
        }
        if (!billingManager.isBoughtOrTrial(request.getId())) {
            context.runOnUiThread(() -> {
                billingManager.showTrialBuyDialog(request.getId());
                drawer.setSelection(lastId);
            });
        } else {
            final String finalName = name;
            context.runOnUiThread(() -> {
                currentFragment = request.getFragment().newInstance();
                if (!context.isFinishing()) {
                    manager.beginTransaction().replace(R.id.content_frame, currentFragment).commitAllowingStateLoss();
                    sharedPref.edit().putString(context.getString(R.string.pref_lastFragment), finalName).apply();
                    lastId = request.getId();
                    ActionBar toolbar = context.getSupportActionBar();
                    if (toolbar != null) {
                        toolbar.setTitle(context.getString(request.getId()));
                    }
                    drawer.setSelection(request.getId());
                }
            });
        }
    }

    public boolean loadLastFragment() {
        int fragment = context.getIntent().getIntExtra(EXTRA_MODE, 0);
        if (fragment != 0) {
            EventBus.getDefault().post(new SwitchFragmentRequest(fragment));
        } else if (sharedPref.contains(context.getString(R.string.pref_lastFragment))) {
            int load = context.getResources().getIdentifier(sharedPref.getString(context.getString(R.string.pref_lastFragment), ""), "id", context.getPackageName());
            EventBus.getDefault().post(new SwitchFragmentRequest(load));
            return true;
        }
        return false;
    }
}
