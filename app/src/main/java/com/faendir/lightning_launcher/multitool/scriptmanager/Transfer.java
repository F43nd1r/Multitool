package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Lukas on 25.08.2015.
 * Object for client communication
 */
@SuppressWarnings("unused")
class Transfer {
    public static final String RENAME = "RENAME";
    public static final String DELETE = "DELETE";
    public static final String RESTORE = "RESTORE";
    public static final String SET_CODE = "SET_CODE";
    public static final String TOGGLE_DISABLE = "TOGGLE_DISABLE";
    @Action public final String request;
    public Script script;

    public Transfer(@Action String request) {
        this.request = request;
    }

    public Transfer(@Action String request, Script script) {
        this.script = script;
        this.request = request;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({RENAME, DELETE, RESTORE, SET_CODE, TOGGLE_DISABLE})
    @interface Action {
    }
}
