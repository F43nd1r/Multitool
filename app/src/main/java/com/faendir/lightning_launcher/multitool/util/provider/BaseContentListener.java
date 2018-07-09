package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
public abstract class BaseContentListener extends ContentObserver {
    private final Context context;
    @NonNull
    private final Uri uri;

    public BaseContentListener(@Nullable Handler handler, @NonNull Context context, @NonNull Uri uri) {
        super(handler);
        this.context = context;
        this.uri = uri;
    }

    @Override
    public abstract void onChange(boolean selfChange);

    public void register() {
        onChange(false);
        context.getContentResolver().registerContentObserver(uri, false, this);
    }

    public void unregister() {
        context.getContentResolver().unregisterContentObserver(this);
    }

    public Context getContext() {
        return context;
    }
}
