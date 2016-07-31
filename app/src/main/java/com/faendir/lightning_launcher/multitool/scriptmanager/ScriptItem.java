package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.support.v7.widget.RecyclerView;

import eu.davidea.flexibleadapter.items.IFlexible;

/**
 * Created by Lukas on 23.08.2015.
 * An item in the ExpandableListView
 */
interface ScriptItem<T extends RecyclerView.ViewHolder> extends IFlexible<T>{
    String getName();
    void setName(String name);
}
