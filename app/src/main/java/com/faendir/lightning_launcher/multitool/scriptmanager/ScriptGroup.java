package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.util.ToStringBuilder;
import com.faendir.omniadapter.Composite;
import com.faendir.omniadapter.DeepObservableList;

import java.util.Collections;

/**
 * Created by Lukas on 22.08.2015.
 * Represents a Group of Scripts
 */
public class ScriptGroup extends Composite<Script> implements Comparable<ScriptGroup>, ScriptItem, Iterable<Script>, DeepObservableList.Listener {

    private String name;
    private final boolean allowDelete;
    private boolean sorted;

    public ScriptGroup(String name, boolean allowDelete) {
        this.name = name;
        this.allowDelete = allowDelete;
        this.addListener(this);
        sorted = false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public boolean allowsDelete() {
        return allowDelete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ScriptGroup listItems = (ScriptGroup) o;

        return allowsDelete() == listItems.allowsDelete() && getName().equals(listItems.getName());

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + (allowsDelete() ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull ScriptGroup another) {
        return getName().toLowerCase().compareTo(another.getName().toLowerCase());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(getName()).append("items", toArray()).build();
    }

    @Override
    public void onListChanged() {
        if (!sorted) {
            beginBatchedUpdates();
            Collections.sort(this);
            endBatchedUpdates();
            sorted = true;
        } else {
            sorted = false;
        }
    }
}
