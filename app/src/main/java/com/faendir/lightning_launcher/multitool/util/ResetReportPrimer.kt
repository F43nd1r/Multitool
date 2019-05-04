package com.faendir.lightning_launcher.multitool.util

import android.content.Context
import android.preference.PreferenceManager
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.R
import com.google.auto.service.AutoService
import org.acra.builder.ReportBuilder
import org.acra.config.CoreConfiguration
import org.acra.config.ReportingAdministrator

/**
 * Created on 25.07.2016.
 *
 * @author F43nd1r
 */
@Keep
@AutoService(ReportingAdministrator::class)
class ResetReportPrimer : ReportingAdministrator {

    override fun shouldStartCollecting(context: Context, config: CoreConfiguration, reportBuilder: ReportBuilder): Boolean {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().putString(context.getString(R.string.pref_lastFragment), null).apply()
        return true
    }
}
