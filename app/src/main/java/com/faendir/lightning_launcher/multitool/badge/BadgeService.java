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

import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.ignoreExceptions;

/**
 * @author F43nd1r
 * @since 25.04.2017
 */

public class BadgeService extends Service {

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return badgeService.asBinder();
    }

    private final IBadgeService badgeService = new IBadgeService.Stub() {
        @Override
        public void registerListener(IBadgeListener listener) throws RemoteException {
            listeners.add(listener);
            String unread = getString(R.string.unread_prefix);
            StreamSupport.stream(sharedPref.getAll().entrySet()).filter(entry -> entry.getKey().startsWith(unread))
                    .forEach(ignoreExceptions(entry -> listener.onCountChange((Integer)entry.getValue(), entry.getKey().substring(unread.length()))));
        }

        @Override
        public void unregisterListener(IBadgeListener listener) throws RemoteException {
            listeners.remove(listener);
        }

        @Override
        public void publish(int count, String packageName) throws RemoteException {
            StreamSupport.stream(listeners).forEach(ignoreExceptions(listener -> listener.onCountChange(count, packageName)));
        }
    };
}
