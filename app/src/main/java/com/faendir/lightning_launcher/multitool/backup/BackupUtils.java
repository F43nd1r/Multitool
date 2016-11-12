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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

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

    @NonNull
    public static String toHumanReadableString(@NonNull Context context, @NonNull BackupTime backupTime) {
        List<Integer> days = new ArrayList<>(backupTime.getDays());
        Collections.sort(days);
        if (days.remove(Integer.valueOf(Calendar.SUNDAY))) days.add(Calendar.SUNDAY);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, backupTime.getHour());
        calendar.set(Calendar.MINUTE, backupTime.getMinute());
        return StreamSupport.stream(days).map((Integer day) -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_WEEK, day);
            return (c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US).substring(0, 2));
        }).collect(Collectors.joining(", ", "", " " + android.text.format.DateFormat.getTimeFormat(context).format(calendar.getTime())));
    }

    public static void scheduleNext(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean shouldEnable = sharedPref.getBoolean(context.getString(R.string.pref_enableBackup), false);
        BackupTime time = BackupUtils.getBackupTime(sharedPref.getString(context.getString(R.string.pref_backupTime), null));
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
