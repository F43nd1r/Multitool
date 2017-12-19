package com.faendir.lightning_launcher.multitool;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

/**
 * Created by Lukas on 13.12.2015.
 * Main Application class
 */
@AcraCore(buildConfigClass = BuildConfig.class,
        reportFormat = StringFormat.JSON)
@AcraHttpSender(uri = "https://faendir.com/acra/report",
        httpMethod = HttpSender.Method.POST,
        basicAuthLogin = "tM7oBAo83wcAmaCK",
        basicAuthPassword = "56Rb0aGfj697yTMG")
public class MultiTool extends Application {
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String LOG_TAG = "[MULTITOOL]";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (DEBUG) {
            ACRA.DEV_LOGGING = true;
        }
        ACRA.init(this);
    }
}
