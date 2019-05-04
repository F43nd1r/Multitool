package com.faendir.lightning_launcher.multitool.backup

import android.content.Context
import android.preference.PreferenceManager
import android.text.format.DateFormat
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.util.Utils.GSON
import com.google.gson.JsonSyntaxException
import java.util.*

/**
 * Created on 23.07.2016.
 *
 * @author F43nd1r
 */

object BackupUtils {
    private const val HOUR_IN_MS = 1000 * 60 * 60
    private val DEFAULT = BackupTime(0, 0, listOf(Calendar.SUNDAY))

    fun getBackupTime(s: String?): BackupTime {
        if(s != null) {
            try {
                return GSON.fromJson<BackupTime>(s, BackupTime::class.java)
            } catch (e : JsonSyntaxException){
            }
        }
        return DEFAULT
    }

    fun toString(backupTime: BackupTime): String {
        return GSON.toJson(backupTime)
    }

    fun toHumanReadableString(context: Context, backupTime: BackupTime): String {
        val days = ArrayList(backupTime.days)
        days.sort()
        if (days.remove(Integer.valueOf(Calendar.SUNDAY))) days.add(Calendar.SUNDAY)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, backupTime.hour)
        calendar.set(Calendar.MINUTE, backupTime.minute)
        return days.joinToString(", ", "", " " + DateFormat.getTimeFormat(context).format(calendar.time)) { day: Int ->
            val c = Calendar.getInstance()
            c.set(Calendar.DAY_OF_WEEK, day)
            c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US).substring(0, 2)
        }
    }

    fun scheduleNext(context: Context) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val enabled = sharedPref.getBoolean(context.getString(R.string.pref_enableBackup), false)
        val time = getBackupTime(sharedPref.getString(context.getString(R.string.pref_backupTime), null))
        if (enabled && time.days.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MINUTE, time.minute)
            calendar.set(Calendar.HOUR_OF_DAY, time.hour)
            if (!Calendar.getInstance().before(calendar)) {
                calendar.add(Calendar.DATE, 1)
            }
            while (!time.days.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                calendar.add(Calendar.DATE, 1)
            }
            val timeMs = calendar.timeInMillis - Calendar.getInstance().timeInMillis
            JobRequest.Builder(BackupJob::class.java.name)
                    .setUpdateCurrent(true)
                    .setExecutionWindow(timeMs, timeMs + if (MultiTool.DEBUG) 1000 else HOUR_IN_MS)
                    .build()
                    .schedule()
        } else {
            JobManager.instance().cancelAllForTag(BackupJob::class.java.name)
        }
    }
}
