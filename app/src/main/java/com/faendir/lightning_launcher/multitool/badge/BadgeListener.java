package com.faendir.lightning_launcher.multitool.badge;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.util.provider.BaseContentListener;

import java9.util.function.Consumer;


/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
public class BadgeListener extends BaseContentListener {
    public BadgeListener(@Nullable Handler handler, @NonNull Context context, String packageName, Consumer<Integer> onChange) {
        super(handler, context, BadgeDataSource.getContentUri(packageName),
                () -> onChange.accept(BadgeDataSource.getBadgeCount(context, packageName)));
    }

}
