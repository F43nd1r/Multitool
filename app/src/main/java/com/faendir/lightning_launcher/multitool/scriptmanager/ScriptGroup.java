package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Created by Lukas on 22.08.2015.
 * Represents a Group of Scripts
 */
public class ScriptGroup extends AbstractExpandableHeaderItem<ScriptGroup.ViewHolder, Script> implements Comparable<ScriptGroup>, ScriptItem<ScriptGroup.ViewHolder>, Iterable<Script> {

    private String name;
    private boolean allowDelete;

    private ScriptGroup(){
        setSelectable(true);
        setEnabled(true);
        if(!hasSubItems())setSubItems(new ArrayList<Script>());
    }

    public ScriptGroup(String name, boolean allowDelete) {
        this();
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
        return getSubItemsCount();
    }

    public Script get(int index) {
        return getSubItem(index);
    }

    public void add(Script s) {
        s.setHeader(this);
        addSubItem(s);
        Collections.sort(getSubItems());
    }

    public boolean remove(Script s) {
        return removeSubItem(s);
    }

    @Override
    public Iterator<Script> iterator() {
        return getSubItems().iterator();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(getName()).append("items", getSubItems()).build();
    }

    @Override
    public int getLayoutRes() {
        return android.R.layout.simple_expandable_list_item_1;
    }

    @Override
    public ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
        holder.getTextView().setText(getName());
        Context context = holder.getTextView().getContext();
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.colorControlHighlight});
        int rippleColor = a.getColor(0, context.getResources().getColor(R.color.primary));
        int normalColor = context.getResources().getColor(android.R.color.transparent);
        int pressedColor = context.getResources().getColor(R.color.accent);
        a.recycle();
        DrawableUtils.setBackground(holder.getTextView(), DrawableUtils.getSelectableBackgroundCompat(rippleColor, normalColor, pressedColor));
    }

    public static class ViewHolder extends ExpandableViewHolder {
        private final TextView textView;

        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            textView = (TextView) view.findViewById(android.R.id.text1);
        }

        public TextView getTextView() {
            return textView;
        }
    }
}
