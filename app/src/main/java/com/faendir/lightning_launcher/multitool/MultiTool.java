package com.faendir.lightning_launcher.multitool;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import com.faendir.lightning_launcher.scriptlib.LightningServiceManager;
import com.google.common.util.concurrent.FutureCallback;
import net.pierrox.lightning_launcher.plugin.IScriptService;
import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraHttpSender;
import org.acra.annotation.AcraLimiter;
import org.acra.annotation.AcraScheduler;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.concurrent.atomic.AtomicInteger;

import static android.app.job.JobInfo.NETWORK_TYPE_UNMETERED;

/**
 * Created by Lukas on 13.12.2015.
 * Main Application class
 */
@AcraCore(buildConfigClass = BuildConfig.class,
        reportFormat = StringFormat.JSON)
@AcraHttpSender(uri = "https://faendir.com/acra/report",
        httpMethod = HttpSender.Method.POST,
        basicAuthLogin = "tM7oBAo83wcAmaCK",
        basicAuthPassword = "56Rb0aGfj697yTMG")
@AcraScheduler(requiresNetworkType = NETWORK_TYPE_UNMETERED,
        requiresBatteryNotLow = true)
@AcraLimiter
public class MultiTool extends Application {
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String LOG_TAG = "[MULTITOOL]";
    private static MultiTool instance;

    private LightningServiceManager serviceManager;
    private AtomicInteger serviceUsers = new AtomicInteger();

    public MultiTool() {
        instance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (DEBUG) {
            ACRA.DEV_LOGGING = true;
        }
        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceManager = new LightningServiceManager(this);
    }

    public static MultiTool get() {
        return instance;
    }

    public void doInLL(LLMethod function) {
        serviceUsers.getAndIncrement();
        serviceManager.getScriptService().addCallback(new FutureCallback<IScriptService>() {
            @Override
            public void onSuccess(@NullableDecl IScriptService result) {
                if (result != null) {
                    function.doInLL(result);
                }
                if (serviceUsers.decrementAndGet() == 0) {
                    serviceManager.closeConnection();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                if (serviceUsers.decrementAndGet() == 0) {
                    serviceManager.closeConnection();
                }
            }
        }, Runnable::run);
    }

    public interface LLMethod {
        void doInLL(@NonNull IScriptService scriptService);
    }
}
