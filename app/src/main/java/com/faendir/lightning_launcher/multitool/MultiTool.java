package com.faendir.lightning_launcher.multitool;

import android.app.Application;
import android.content.Context;

import com.faendir.lightning_launcher.multitool.util.ResetReportPrimer;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Created by Lukas on 13.12.2015.
 * Main Application class
 */
@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "https://faendir.smileupps.com/acra-multitool/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "multitool",
        formUriBasicAuthPassword = "mtR3p0rt",
        reportPrimerClass = ResetReportPrimer.class,
        buildConfigClass = BuildConfig.class
)
public class MultiTool extends Application {
    public static final boolean DEBUG = true;
    public static final String LOG_TAG = "[MULTITOOL]";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if(DEBUG){
            ACRA.DEV_LOGGING = true;
        }
        ACRA.init(this);
    }
}
