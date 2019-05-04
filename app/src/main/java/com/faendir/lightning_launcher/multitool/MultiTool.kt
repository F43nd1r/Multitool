package com.faendir.lightning_launcher.multitool

import android.app.Application
import android.app.job.JobInfo.NETWORK_TYPE_UNMETERED
import android.content.Context
import com.evernote.android.job.JobManager
import com.faendir.lightning_launcher.scriptlib.LightningServiceManager
import com.google.common.util.concurrent.FutureCallback
import net.pierrox.lightning_launcher.plugin.IScriptService
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraHttpSender
import org.acra.annotation.AcraLimiter
import org.acra.annotation.AcraScheduler
import org.acra.data.StringFormat
import org.acra.sender.HttpSender
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Lukas on 13.12.2015.
 * Main Application class
 */
@AcraCore(buildConfigClass = BuildConfig::class, reportFormat = StringFormat.JSON)
@AcraHttpSender(uri = "https://acra.faendir.com/report", httpMethod = HttpSender.Method.POST, basicAuthLogin = "tM7oBAo83wcAmaCK", basicAuthPassword = "56Rb0aGfj697yTMG")
@AcraScheduler(requiresNetworkType = NETWORK_TYPE_UNMETERED, requiresBatteryNotLow = true)
@AcraLimiter
class MultiTool : Application() {

    private lateinit var serviceManager: LightningServiceManager
    private val serviceUsers = AtomicInteger()

    init {
        instance = this
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (DEBUG) {
            ACRA.DEV_LOGGING = true
        }
        ACRA.init(this)
    }

    override fun onCreate() {
        super.onCreate()
        JobManager.create(this)
        serviceManager = LightningServiceManager(this)
    }

    fun doInLL(function: (IScriptService) -> Unit) {
        serviceUsers.getAndIncrement()
        serviceManager.scriptService.addCallback(object : FutureCallback<IScriptService> {
            override fun onSuccess(result: IScriptService?) {
                if (result != null) {
                    function.invoke(result)
                }
                if (serviceUsers.decrementAndGet() == 0) {
                    serviceManager.closeConnection()
                }
            }

            override fun onFailure(t: Throwable) {
                t.printStackTrace()
                if (serviceUsers.decrementAndGet() == 0) {
                    serviceManager.closeConnection()
                }
            }
        }) { it.run() }
    }

    companion object {
        val DEBUG = BuildConfig.DEBUG
        const val LOG_TAG = "[MULTITOOL]"
        private lateinit var instance: MultiTool

        fun get(): MultiTool {
            return instance
        }
    }
}
