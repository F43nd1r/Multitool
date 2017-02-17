package com.faendir.lightning_launcher.multitool.scriptmanager;

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


    public Script script;
    public final String request;

    public Transfer(String request){
        this.request = request;
    }
}
