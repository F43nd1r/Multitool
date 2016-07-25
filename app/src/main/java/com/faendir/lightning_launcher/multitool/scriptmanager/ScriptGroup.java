package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.util.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Lukas on 22.08.2015.
 * Represents a Group of Scripts
 */
public class ScriptGroup implements Comparable<ScriptGroup>, ScriptItem, Iterable<Script> {

    private String name;
    private final boolean allowDelete;
    private final List<Script> items;

    public ScriptGroup(String name, boolean allowDelete) {
        this.name = name;
        this.allowDelete = allowDelete;
        items = new ArrayList<>();
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

    public int size() {
        return items.size();
    }

    public Script get(int index) {
        return items.get(index);
    }

    public void add(Script s) {
        items.add(s);
        Collections.sort(items);
    }

    public boolean remove(Script s) {
        return items.remove(s);
    }

    @Override
    public Iterator<Script> iterator() {
        return items.iterator();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(getName()).append("items", items).build();
    }
}
