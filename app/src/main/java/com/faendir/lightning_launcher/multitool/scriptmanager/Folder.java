package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.support.annotation.NonNull;

import com.faendir.omniadapter.model.DeepObservableList;
import com.faendir.omniadapter.model.SimpleComposite;

/**
 * @author F43nd1r
 * @since 29.10.2016
 */

public class Folder extends SimpleComposite<ScriptItem> implements ScriptItem, Comparable<Folder> {
    private String name;

    public Folder(String name) {
        super(ScriptItem.class);
        this.name = name;
    }

    @Override
    public String getName() {
        DeepObservableList<ScriptItem> children = super.getChildren();
        if (children.size() == 1 && children.get(0) instanceof Folder) {
            return name + "/" + children.get(0).getName();
        }
        return name;
    }

    public String getRealName(){
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DeepObservableList<ScriptItem> getChildren() {
        DeepObservableList<ScriptItem> children = super.getChildren();
        if (children.size() == 1 && children.get(0) instanceof Folder) {
            return ((Folder) children.get(0)).getChildren();
        }
        return super.getChildren();
    }

    public DeepObservableList<ScriptItem> getRealChildren(){
        return super.getChildren();
    }

    @Override
    public int compareTo(@NonNull Folder o) {
        return name.compareTo(o.getName());
    }
}
