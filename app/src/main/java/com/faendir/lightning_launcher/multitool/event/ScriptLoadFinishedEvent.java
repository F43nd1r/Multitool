package com.faendir.lightning_launcher.multitool.event;

/**
 * Created by Lukas on 01.04.2016.
 */
public class ScriptLoadFinishedEvent {
    private final int id;

    public ScriptLoadFinishedEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
