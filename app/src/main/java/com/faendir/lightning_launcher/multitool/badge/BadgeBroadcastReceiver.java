package com.faendir.lightning_launcher.multitool.badge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.faendir.lightning_launcher.multitool.R;

import java8.util.Optional;

import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.ignoreExceptions;

/**
 * @author F43nd1r
 * @since 25.04.2017
 */

public class BadgeBroadcastReceiver extends BroadcastReceiver {
    private static final String BADGE_COUNT = "badge_count";
    private static final String PACKAGE_NAME = "badge_count_package_name";
    private static final String INTENT_BADGE_COUNT_UPDATE = "android.intent.action.BADGE_COUNT_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(INTENT_BADGE_COUNT_UPDATE)) {
            if (intent.hasExtra(BADGE_COUNT) && intent.hasExtra(PACKAGE_NAME)) {
                String packageName = intent.getStringExtra(PACKAGE_NAME);
                int count = intent.getIntExtra(BADGE_COUNT, 0);
                PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(context.getString(R.string.unread_prefix) + packageName, count).apply();
                Optional.ofNullable(peekService(context, new Intent(context, BadgeService.class)))
                        .ifPresent(ignoreExceptions(binder -> IBadgeService.Stub.asInterface(binder).publish(count, packageName)));
            }
        }
    }
}
