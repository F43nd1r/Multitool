package com.faendir.lightning_launcher.multitool.backup

import androidx.annotation.Keep
import com.evernote.android.job.Job
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.JavaScript
import com.faendir.lightning_launcher.multitool.util.Utils
import com.faendir.lightning_launcher.scriptlib.LightningServiceManager
import net.pierrox.lightning_launcher.api.ScreenIdentity

import java.util.concurrent.ExecutionException

/**
 * @author F43nd1r
 * @since 30.01.2018
 */
@Keep
class BackupJob : Job() {
    override fun onRunJob(params: Params): Result {
        val lightningServiceManager = LightningServiceManager(context)
        try {
            lightningServiceManager.scriptService.get().runCode("var " + JavaScript.Direct.PARAM_CLASS + " = " + BackupCreator::class.java.name + "\n" + Utils.readRawResource(context, R.raw.direct), ScreenIdentity.BACKGROUND)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        BackupUtils.scheduleNext(context)
        return Result.SUCCESS
    }
}
