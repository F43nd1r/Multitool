package com.faendir.lightning_launcher.multitool.badge;

import android.os.RemoteException;

import java8.util.function.BiConsumer;

/**
 * @author F43nd1r
 * @since 26.04.2017
 */

@SuppressWarnings("unused")
public class BadgeListener extends IBadgeListener.Stub {

    private BiConsumer<Integer, String> consumer;

    public BadgeListener(){
    }
    @Override
    public void onCountChange(int newCount, String packageName) throws RemoteException {
        consumer.accept(newCount, packageName);
    }

    public void setConsumer(BiConsumer<Integer, String> consumer) {
        this.consumer = consumer;
    }
}
