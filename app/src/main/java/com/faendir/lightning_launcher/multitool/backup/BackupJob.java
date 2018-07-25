package com.faendir.lightning_launcher.multitool.backup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.evernote.android.job.Job;
import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.executor.DirectScriptExecutor;

/**
 * @author F43nd1r
 * @since 30.01.2018
 */
@Keep
public class BackupJob extends Job {
    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        ScriptManager manager = new ScriptManager(getContext());
        if (MultiTool.DEBUG) manager.enableDebug();
        manager.getAsyncExecutorService()
                .add(new DirectScriptExecutor(R.raw.direct).putVariable(JavaScript.Direct.PARAM_CLASS, BackupCreator.class.getName()), s -> {
                    synchronized (this) {
                        this.notifyAll();
                    }
                })
                .start();
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException ignored) {
            }
        }
        BackupUtils.scheduleNext(getContext());
        return Result.SUCCESS;
    }
}
