package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.Loader;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.DeletableModel;


/**
 * Created by Lukas on 22.08.2015.
 * Represents a script
 */
public class Script implements Comparable<Script>, DeletableModel {
    @SuppressWarnings("unused")
    private int id;
    private String name;
    private String code;
    private int flags;
    private String path;

    public Script(String name, int id, String code, int flags, String path) {
        this.name = name;
        this.id = id;
        this.code = code;
        this.flags = flags;
        this.path = path;
    }

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

    public String getCode() {
        return code;
    }

    public int getFlags() {
        return flags;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getUndoText(@NonNull Context context) {
        return context.getString(R.string.text_scriptDeleted);
    }

    @Override
    public int getTintColor(@NonNull Context context) {
        return isDisabled() ? Color.RED : Color.WHITE;
    }

    public boolean isDisabled() {
        return (flags & Loader.FLAG_DISABLED) != 0;
    }

    @Override
    public Drawable getIcon(@NonNull Context context) {
        return context.getResources().getDrawable(R.drawable.ic_file_white);
    }

    @Override
    public String toString() {
        return "Script{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
