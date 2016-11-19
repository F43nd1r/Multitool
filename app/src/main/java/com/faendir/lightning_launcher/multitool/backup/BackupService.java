package com.faendir.lightning_launcher.multitool.backup;

import android.app.IntentService;
import android.content.Intent;

import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.executor.DirectScriptExecutor;

/**
 * Created on 16.07.2016.
 *
 * @author F43nd1r
 */

public class BackupService extends IntentService {
    public BackupService() {
        super(BackupService.class.getName());
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ScriptManager manager = new ScriptManager(this);
        if (MultiTool.DEBUG) manager.enableDebug();
        manager.getAsyncExecutorService()
                .add(new DirectScriptExecutor(R.raw.backup))
                .start();
        BackupUtils.scheduleNext(this);
    }
}
