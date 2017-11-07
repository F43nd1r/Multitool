package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

public class BaseContentListener extends ContentObserver {
    private final Context context;
    @NonNull
    private final Uri uri;
    private final Runnable onChange;

    public BaseContentListener(@Nullable Handler handler, @NonNull Context context, @NonNull Uri uri, Runnable onChange) {
        super(handler);
        this.context = context;
        this.uri = uri;
        this.onChange = onChange;
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange.run();
    }

    public void register() {
        context.getContentResolver().registerContentObserver(uri, false, this);
    }

    public void unregister() {
        context.getContentResolver().unregisterContentObserver(this);
    }

    public Context getContext() {
        return context;
    }
}
