package com.faendir.lightning_launcher.multitool.backup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.evernote.android.job.Job;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.scriptlib.LightningServiceManager;
import net.pierrox.lightning_launcher.api.ScreenIdentity;

import java.util.concurrent.ExecutionException;

/**
 * @author F43nd1r
 * @since 30.01.2018
 */
@Keep
public class BackupJob extends Job {
    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        LightningServiceManager lightningServiceManager = new LightningServiceManager(getContext());
        try {
            lightningServiceManager.getScriptService().get().runCode("var " + JavaScript.Direct.PARAM_CLASS+ " = " + BackupCreator.class.getName() + "\n" + Utils.readRawResource(getContext(), R.raw.direct), ScreenIdentity.BACKGROUND);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        BackupUtils.scheduleNext(getContext());
        return Result.SUCCESS;
    }
}
