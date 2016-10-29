package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.support.annotation.NonNull;

import com.faendir.omniadapter.model.Leaf;


/**
 * Created by Lukas on 22.08.2015.
 * Represents a script
 */
public class Script extends Leaf implements Comparable<Script>, ScriptItem {
    private int id;
    private String name;
    private String code;
    private int flags;
    private String path;

    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Script script = (Script) o;

        return getId() == script.getId();

    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public int compareTo(@NonNull Script another) {
        return getName().toLowerCase().compareTo(another.getName().toLowerCase());
    }

    private void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public int getFlags() {
        return flags;
    }

    public void fillFrom(Script script){
        setName(script.getName());
        setId(script.getId());
        setCode(script.getCode());
        setFlags(script.getFlags());
        setPath(script.getPath());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
