package com.faendir.lightning_launcher.multitool.event;

import com.faendir.lightning_launcher.scriptlib.ErrorCode;

/**
 * Created by Lukas on 01.04.2016.
 */
public class ScriptLoadFailedEvent {
    private final ErrorCode errorCode;

    public ScriptLoadFailedEvent(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
