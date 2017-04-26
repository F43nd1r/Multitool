package com.faendir.lightning_launcher.multitool.badge;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.R;

import java.util.HashSet;
import java.util.Set;

import java8.util.stream.StreamSupport;

/**
 * @author F43nd1r
 * @since 25.04.2017
 */

public class BadgeService extends Service {
    public static final String BADGE_COUNT = "badge_count";
    public static final String PACKAGE_NAME = "badge_count_package_name";
    public static final String WHATSAPP = "com.whatsapp";
    public static final String INTENT_BADGE_COUNT_UPDATE = "android.intent.action.BADGE_COUNT_UPDATE";

    private SharedPreferences sharedPref;
    private final Set<IBadgeListener> listeners;

    public BadgeService() {
        this.listeners = new HashSet<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction().equals(INTENT_BADGE_COUNT_UPDATE)){
            if (intent.hasExtra(BADGE_COUNT) && intent.hasExtra(PACKAGE_NAME)) {
                String packageName = intent.getStringExtra(PACKAGE_NAME);
                if (WHATSAPP.equals(packageName)) {
                    int count = intent.getIntExtra(BADGE_COUNT, 0);
                    sharedPref.edit().putInt(getString(R.string.unread_whatsapp), count).apply();
                    StreamSupport.stream(listeners).forEach(listener -> {
                        try {
                            listener.onCountChange(count, packageName);
                        } catch (RemoteException ignored) {
                        }
                    });
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return badgeService.asBinder();
    }

    private final IBadgeService badgeService = new IBadgeService.Stub() {
        @Override
        public void registerListener(IBadgeListener listener) throws RemoteException {
            listeners.add(listener);
            listener.onCountChange(sharedPref.getInt(getString(R.string.unread_whatsapp), 0), WHATSAPP);
        }

        @Override
        public void unregisterListener(IBadgeListener listener) throws RemoteException {
            listeners.remove(listener);
        }
    };
}
