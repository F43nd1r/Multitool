package com.faendir.lightning_launcher.multitool.util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author F43nd1r
 * @since 25.04.2017
 */

public abstract class BaseIpcService extends Service {
    private final Messenger messenger;

    public BaseIpcService() {
        messenger = new Messenger(new LocalHandler(this));
    }

    @CallSuper
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    protected abstract void handleMessage(Message msg);

    private static class LocalHandler extends Handler{
        private final WeakReference<BaseIpcService> service;

        private LocalHandler(BaseIpcService service) {
            super(getNewPreparedLooper());
            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseIpcService service = this.service.get();
            if (service != null) {
                service.handleMessage(msg);
            }
        }

        private static Looper getNewPreparedLooper() {
            final AtomicReference<Looper> reference = new AtomicReference<>();
            new Thread(() -> {
                Looper.prepare();
                synchronized (reference) {
                    reference.set(Looper.myLooper());
                    reference.notifyAll();
                }
                Looper.loop();
            }).start();
            while (reference.get() == null) {
                synchronized (reference) {
                    try {
                        reference.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            return reference.get();
        }
    }
}
