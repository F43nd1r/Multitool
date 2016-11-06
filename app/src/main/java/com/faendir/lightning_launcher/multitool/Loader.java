package com.faendir.lightning_launcher.multitool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;

import com.faendir.lightning_launcher.multitool.billing.BillingManager;
import com.faendir.lightning_launcher.multitool.util.BaseActivity;

/**
 * @author F43nd1r
 * @since 26.10.2016
 */

public class Loader extends Activity {
    private static final String PKG = "com.faendir.lightning_launcher.multitool";
    private static final String LAUNCHER_SCRIPT = PKG + ".LoadLauncherScript";
    private static final String GESTURE_LAUNCHER = PKG + ".LoadGestureLauncher";
    private static final String MUSIC_WIDGET = PKG + ".LoadMusicWidget";
    private static final String DRAWER = PKG + ".LoadDrawer";

    public static final int FLAG_APP_MENU = 2;
    public static final int FLAG_ITEM_MENU = 4;
    public static final int FLAG_CUSTOM_MENU = 8;
    private static final String INTENT_EXTRA_SCRIPT_ID = "i";
    private static final String INTENT_EXTRA_SCRIPT_FLAGS = "f";
    private static final String INTENT_EXTRA_SCRIPT_NAME = "n";
    private static final String INTENT_EXTRA_EXECUTE_ON_LOAD = "e";
    private static final String INTENT_EXTRA_DELETE_AFTER_EXECUTION = "d";
    private static final String INTENT_EXTRA_SCRIPT_PACKAGE = "p";

    private BillingManager billingManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        billingManager = new BillingManager(this);
        switch (getIntent().getComponent().getClassName()) {
            case LAUNCHER_SCRIPT:
                check(R.string.title_launcherScript, R.raw.multitool, false, FLAG_APP_MENU + FLAG_ITEM_MENU, getString(R.string.script_name), true);
                break;
            case GESTURE_LAUNCHER:
                setupCheck(R.string.title_gestureLauncher, R.raw.gesture_setup);
                break;
            case MUSIC_WIDGET:
                setupCheck(R.string.title_musicWidget, R.raw.music_setup);
                break;
            case DRAWER:
                setupCheck(R.string.title_drawer, R.raw.drawer_setup);
                break;
        }
    }

    private void setupCheck(@StringRes final int id, @RawRes final int script) {
        check(id, script, true, 0, null, true);
    }

    private void check(@StringRes final int id, @RawRes final int script, final boolean runAndDelete, final int flags, final String name, final boolean showDialog) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (billingManager.isBoughtOrTrial(id)) {
                    setResult(script, runAndDelete, flags, name);
                } else if (showDialog) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            billingManager.showDialog(id, new Runnable() {
                                @Override
                                public void run() {
                                    check(id, script, runAndDelete, flags, name, false);
                                }
                            });
                        }
                    });
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }).start();
    }

    private void setResult(@RawRes int script, boolean runAndDelete, int flags, String name) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_SCRIPT_ID, script);
        intent.putExtra(INTENT_EXTRA_SCRIPT_NAME, name);
        intent.putExtra(INTENT_EXTRA_SCRIPT_PACKAGE, PKG.replace('.', '/'));
        intent.putExtra(INTENT_EXTRA_SCRIPT_FLAGS, flags);
        intent.putExtra(INTENT_EXTRA_EXECUTE_ON_LOAD, runAndDelete);
        intent.putExtra(INTENT_EXTRA_DELETE_AFTER_EXECUTION, runAndDelete);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (billingManager != null) {
            billingManager.release();
        }
        super.onDestroy();
    }
}
