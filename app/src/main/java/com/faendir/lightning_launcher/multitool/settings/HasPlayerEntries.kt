package com.faendir.lightning_launcher.multitool.settings

import android.content.Intent
import android.content.pm.PackageManager

/**
 * @author lukas
 * @since 21.06.18
 */
interface HasPlayerEntries {
    fun setEntries(entries: Array<CharSequence>)

    fun setEntryValues(entryValues: Array<CharSequence>)

    fun discoverPlayers(pm: PackageManager) {
        val infos = pm.queryBroadcastReceivers(Intent(Intent.ACTION_MEDIA_BUTTON), 0)
        setEntries(infos.map { info -> info.activityInfo.loadLabel(pm).toString() }.toTypedArray())
        setEntryValues(infos.map { info -> info.activityInfo.packageName }.toTypedArray())
    }
}
