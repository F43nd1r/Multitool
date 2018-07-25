package com.faendir.lightning_launcher.multitool.fastadapter;

import android.app.ActivityManager;
import android.content.Context;
import androidx.annotation.NonNull;

/**
 * @author F43nd1r
 * @since 26.12.2017
 */

public class ItemFactory<T extends Model> {

    private final int size;

    public static <T extends Model> ItemFactory<T> forLauncherIconSize(@NonNull Context context){
        //noinspection ConstantConditions
        return new ItemFactory<>(((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getLauncherLargeIconSize());
    }

    public ItemFactory(int size) {
        this.size = size;
    }

    public ExpandableItem<T> wrap(T item) {
        return new ExpandableItem<>(item, size);
    }
}
