package com.faendir.lightning_launcher.multitool;

import android.app.Application;
import android.content.Context;

import com.faendir.lightning_launcher.multitool.util.ResetReportPrimer;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.sender.HttpSender;

/**
 * Created by Lukas on 13.12.2015.
 * Main Application class
 */
@AcraCore(buildConfigClass = BuildConfig.class,
        reportPrimerClass = ResetReportPrimer.class)
@AcraHttpSender(uri = "https://faendir.com/acra/report",
        httpMethod = HttpSender.Method.POST,
        reportType = HttpSender.Type.JSON,
        basicAuthLogin = "591c604b42110637a8e132c0",
        basicAuthPassword = "CTgrnKHPk1hjDNX4")
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
