package com.faendir.lightning_launcher.multitool.backup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.faendir.lightning_launcher.multitool.R;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Calendar;
import java.util.Collections;

/**
 * Created on 23.07.2016.
 *
 * @author F43nd1r
 */

public class BackupUtils {
    private static final BackupTime DEFAULT = new BackupTime(0, 0, Collections.singletonList(Calendar.SUNDAY));
    private static final Gson GSON = new Gson();

    @NonNull
    public static BackupTime getBackupTime(@Nullable String s) {
        if (s != null && !TextUtils.isEmpty(s)) {
            try {
                return GSON.fromJson(s, BackupTime.class);
            } catch (JsonSyntaxException ignored) {
            }
        }
        return DEFAULT;
    }

    @NonNull
    public static String toString(@NonNull BackupTime backupTime) {
        return GSON.toJson(backupTime);
    }

    public static void scheduleNext(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean shouldEnable = sharedPref.getBoolean(context.getString(R.string.key_enableBackup), false);
        BackupTime time = BackupUtils.getBackupTime(sharedPref.getString(context.getString(R.string.key_backupTime), null));
        PendingIntent intent = PendingIntent.getService(context, 0, new Intent(context, BackupService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        if (shouldEnable) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MINUTE, time.getMinute());
            calendar.set(Calendar.HOUR, time.getHour());
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }
            while (!time.getDays().contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                calendar.add(Calendar.DATE, 1);
            }
            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), intent);
        } else {
            intent.cancel();
        }
    }
}
