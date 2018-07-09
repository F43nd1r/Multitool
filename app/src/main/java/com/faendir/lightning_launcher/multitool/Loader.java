package com.faendir.lightning_launcher.multitool;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;
import com.faendir.lightning_launcher.multitool.animation.AnimationScript;
import com.faendir.lightning_launcher.multitool.badge.BadgeSetup;
import com.faendir.lightning_launcher.multitool.billing.BillingManager;
import com.faendir.lightning_launcher.multitool.drawer.Drawer;
import com.faendir.lightning_launcher.multitool.gesture.GestureScript;
import com.faendir.lightning_launcher.multitool.immersive.ImmersiveScript;
import com.faendir.lightning_launcher.multitool.music.MusicSetup;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.util.LambdaUtils;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;
import static com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG;
import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.exceptionToOptional;

/**
 * @author F43nd1r
 * @since 26.10.2016
 */

public class Loader extends Activity {
    private static final String PKG = BuildConfig.APPLICATION_ID;
    private static final String LAUNCHER_SCRIPT = PKG + ".LoadLauncherScript";
    private static final String GESTURE_LAUNCHER = PKG + ".LoadGestureLauncher";
    private static final String MUSIC_WIDGET = PKG + ".LoadMusicWidget";
    private static final String DRAWER = PKG + ".LoadDrawer";
    private static final String IMMERSIVE = PKG + ".toggleImmersive";
    private static final String ANIMATION = PKG + ".LoadAnimation";
    private static final String BADGE = PKG + ".LoadBadge";

    public static final int FLAG_DISABLED = 1;
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

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        billingManager = new BillingManager(this);
        if (DEBUG) Log.d(LOG_TAG, "Loader for class " + getIntent().getComponent().getClassName());
        switch (getIntent().getComponent().getClassName()) {
            case LAUNCHER_SCRIPT:
                check(R.string.title_launcherScript, R.raw.multitool, false, FLAG_APP_MENU + FLAG_ITEM_MENU, getString(R.string.script_name), true);
                break;
            case GESTURE_LAUNCHER:
                setupCheck(R.string.title_gestureLauncher, GestureScript.class);
                break;
            case MUSIC_WIDGET:
                setupCheck(R.string.title_musicWidget, MusicSetup.class);
                break;
            case DRAWER:
                setupCheck(R.string.title_drawer, Drawer.class);
                break;
            case IMMERSIVE:
                setupCheck(R.string.title_immersive, ImmersiveScript.class);
                break;
            case ANIMATION:
                setupCheck(R.string.title_animation, AnimationScript.class);
                break;
            case BADGE:
                setupCheck(R.string.title_badge, BadgeSetup.class);
                break;
        }
    }

    private void setupCheck(@StringRes final int id, Class<? extends JavaScript.Setup> clazz) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(getString(R.string.pref_setupClass), clazz.getName()).apply();
        check(id, R.raw.setup, true, 0, null, true);
    }

    private void check(@StringRes final int id, @RawRes final int script, final boolean runAndDelete, final int flags, final String name, final boolean showDialog) {
        if (checkLightningVersion()) {
            new Thread(() -> {
                if (billingManager.isBoughtOrTrial(id)) {
                    setResult(script, runAndDelete, flags, name);
                } else if (showDialog) {
                    runOnUiThread(() -> billingManager.showTrialBuyDialog(id, () -> check(id, script, runAndDelete, flags, name, false)));
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }).start();
        } else {
            Toast.makeText(this, "Lightning Launcher is outdated, please update!", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
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

    private boolean checkLightningVersion() {
        PackageManager pm = getPackageManager();
        return exceptionToOptional((LambdaUtils.ExceptionalBiFunction<String, Integer, PackageInfo, PackageManager.NameNotFoundException>)pm::getPackageInfo).apply("net.pierrox.lightning_launcher_extreme", 0)
                .or(() -> exceptionToOptional((LambdaUtils.ExceptionalBiFunction<String, Integer, PackageInfo, PackageManager.NameNotFoundException>)pm::getPackageInfo).apply("net.pierrox.lightning_launcher", 0))
                .map(info -> info.versionCode % 1000 >= 307).orElse(false);
    }
}
