package com.faendir.lightning_launcher.multitool.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import java.util.*

/**
 * Created on 12.07.2016.
 *
 * @author F43nd1r
 */

internal class IntentHandlerListTask(context: Context, private val intent: Intent, private val isIndirect: Boolean, private val postExecute: (List<IntentInfo>)->Unit) : AsyncTask<Void, Void, List<IntentInfo>>() {

    private val pm: PackageManager = context.packageManager

    override fun doInBackground(vararg params: Void): List<IntentInfo> {
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val infos = ArrayList<IntentInfo>()
        for (resolveInfo in resolveInfos) {
            val activity = resolveInfo.activityInfo
            val name = ComponentName(activity.applicationInfo.packageName,
                    activity.name)
            val launchIntent = Intent(intent)
            launchIntent.component = name
            val intentInfo = IntentInfo(launchIntent, ResolveInfoDrawableProvider(pm, resolveInfo), resolveInfo.loadLabel(pm).toString(), isIndirect)
            var found = false
            for (info in infos) {
                if (info.intent.component!!.packageName == intentInfo.intent.component!!.packageName && info.name == intentInfo.name) {
                    found = true
                    break
                }
            }
            if (!found) {
                infos.add(intentInfo)
            }
        }
        return infos
    }

    override fun onPostExecute(infos: List<IntentInfo>) {
        postExecute.invoke(infos)
    }
}
