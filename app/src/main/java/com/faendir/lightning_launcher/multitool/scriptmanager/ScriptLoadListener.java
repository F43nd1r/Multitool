package com.faendir.lightning_launcher.multitool.scriptmanager;

import com.faendir.lightning_launcher.multitool.event.ScriptLoadFailedEvent;
import com.faendir.lightning_launcher.multitool.event.ScriptLoadFinishedEvent;
import com.faendir.lightning_launcher.scriptlib.ErrorCode;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Lukas on 01.04.2016.
 */
public class ScriptLoadListener extends ScriptManager.Listener {

    @Override
    public void onError(ErrorCode errorCode) {
        EventBus.getDefault().post(new ScriptLoadFailedEvent(errorCode));
    }

    @Override
    public void onLoadFinished(int id) {
        EventBus.getDefault().post(new ScriptLoadFinishedEvent(id));
    }
}
