package com.faendir.lightning_launcher.multitool;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Created by Lukas on 13.12.2015.
 */
@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "http://acra-c56dce.smileupps.com/acra-multitool/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "multitool",
        formUriBasicAuthPassword = "mtR3p0rt"
)
public class MultiTool extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
