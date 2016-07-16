package com.faendir.lightning_launcher.multitool.backup;

import android.app.IntentService;
import android.content.Intent;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.exception.RepositoryImporterException;

/**
 * Created by Lukas on 16.07.2016.
 */

public class BackupService extends IntentService {
    public BackupService() {
        super(BackupService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ScriptManager scriptManager = new ScriptManager(this);
        try {
            scriptManager.bind();
            scriptManager.runScriptForResult(R.raw.backup, null);
            scriptManager.unbind();
        } catch (RepositoryImporterException e) {
            e.printStackTrace();
        }
    }
}
