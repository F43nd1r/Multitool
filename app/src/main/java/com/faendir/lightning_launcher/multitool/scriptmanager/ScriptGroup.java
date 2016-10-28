package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.util.ToStringBuilder;
import com.faendir.omniadapter.DeepObservableList;
import com.faendir.omniadapter.model.ChangeInformation;
import com.faendir.omniadapter.model.Composite;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Lukas on 22.08.2015.
 * Represents a Group of Scripts
 */
public class ScriptGroup extends Composite<Script> implements Comparable<ScriptGroup>, ScriptItem, Iterable<Script>, DeepObservableList.Listener<ScriptItem> {

    private String name;
    private final boolean allowDelete;

    public ScriptGroup(String name, boolean allowDelete) {
        getChildren().addListener(this);
        this.name = name;
        this.allowDelete = allowDelete;
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
        return new ToStringBuilder(getName()).append("items", getChildren().toArray()).build();
    }

    @Override
    public void onListChanged(List<ChangeInformation<ScriptItem>> changeInfo) {
        Collections.sort(getChildren());
    }

    @Override
    public Iterator<Script> iterator() {
        return getChildren().iterator();
    }
}
