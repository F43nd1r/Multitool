package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.faendir.lightning_launcher.multitool.R;

import org.acra.builder.ReportBuilder;
import org.acra.builder.ReportPrimer;

/**
 * Created on 25.07.2016.
 *
 * @author F43nd1r
 */

public class ResetReportPrimer implements ReportPrimer {
    @Override
    public void primeReport(Context context, ReportBuilder builder) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putString(context.getString(R.string.pref_lastFragment), null).apply();
    }
}
