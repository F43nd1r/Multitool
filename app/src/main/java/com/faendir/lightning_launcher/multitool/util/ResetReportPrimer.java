package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.R;
import com.google.auto.service.AutoService;

import org.acra.builder.ReportBuilder;
import org.acra.config.CoreConfiguration;
import org.acra.config.ReportingAdministrator;
import org.acra.data.CrashReportData;

/**
 * Created on 25.07.2016.
 *
 * @author F43nd1r
 */
@Keep
@AutoService(ReportingAdministrator.class)
public class ResetReportPrimer implements ReportingAdministrator {

    public ResetReportPrimer(){
    }

    @Override
    public boolean shouldStartCollecting(@NonNull Context context, @NonNull CoreConfiguration config, @NonNull ReportBuilder reportBuilder) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putString(context.getString(R.string.pref_lastFragment), null).apply();
        return true;
    }
}
