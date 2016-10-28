package com.faendir.lightning_launcher.multitool;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;

import com.faendir.lightning_launcher.multitool.util.BaseActivity;

/**
 * @author F43nd1r
 * @since 26.10.2016
 */

public class Loader extends BaseActivity {
    private static final String PKG = "com.faendir.lightning_launcher.multitool";
    private static final String LAUNCHER_SCRIPT = PKG + ".LoadLauncherScript";
    private static final String GESTURE_LAUNCHER = PKG + ".LoadGestureLauncher";
    private static final String MUSIC_WIDGET = PKG + ".LoadMusicWidget";

    public static final int FLAG_APP_MENU = 2;
    public static final int FLAG_ITEM_MENU = 4;
    public static final int FLAG_CUSTOM_MENU = 8;
    private static final String INTENT_EXTRA_SCRIPT_ID = "i";
    private static final String INTENT_EXTRA_SCRIPT_FLAGS = "f";
    private static final String INTENT_EXTRA_SCRIPT_NAME = "n";
    private static final String INTENT_EXTRA_EXECUTE_ON_LOAD = "e";
    private static final String INTENT_EXTRA_DELETE_AFTER_EXECUTION = "d";
    private static final String INTENT_EXTRA_SCRIPT_PACKAGE = "p";

    public Loader() {
        super(R.layout.content_empty);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @RawRes int script;
        boolean runAndDelete;
        int flags = 0;
        String name = null;
        switch (getIntent().getComponent().getClassName()) {
            case LAUNCHER_SCRIPT:
                script = R.raw.multitool;
                runAndDelete = false;
                flags = FLAG_APP_MENU + FLAG_ITEM_MENU;
                name = getString(R.string.script_name);
                break;
            case GESTURE_LAUNCHER:
                script = R.raw.gesture_setup;
                runAndDelete = true;
                break;
            case MUSIC_WIDGET:
                script = R.raw.music_setup;
                runAndDelete = true;
                break;
            default:
                return;
        }
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_SCRIPT_ID, script);
        intent.putExtra(INTENT_EXTRA_SCRIPT_NAME, name);
        intent.putExtra(INTENT_EXTRA_SCRIPT_PACKAGE, PKG.replace('.','/'));
        intent.putExtra(INTENT_EXTRA_SCRIPT_FLAGS, flags);
        intent.putExtra(INTENT_EXTRA_EXECUTE_ON_LOAD, runAndDelete);
        intent.putExtra(INTENT_EXTRA_DELETE_AFTER_EXECUTION, runAndDelete);
        setResult(RESULT_OK, intent);
        finish();
    }
}
