package com.faendir.lightning_launcher.multitool.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import androidx.annotation.NonNull;
import java9.util.stream.StreamSupport;

import java.util.List;

/**
 * @author lukas
 * @since 21.06.18
 */
public interface HasPlayerEntries {
    void setEntries(CharSequence[] entries);

    void setEntryValues(CharSequence[] entryValues);

    default void discoverPlayers(@NonNull PackageManager pm) {
        List<ResolveInfo> infos = pm.queryBroadcastReceivers(new Intent(Intent.ACTION_MEDIA_BUTTON), 0);
        setEntries(StreamSupport.stream(infos).map(info -> info.activityInfo.loadLabel(pm).toString()).toArray(String[]::new));
        setEntryValues(StreamSupport.stream(infos).map(info -> info.activityInfo.packageName).toArray(String[]::new));
    }
}
