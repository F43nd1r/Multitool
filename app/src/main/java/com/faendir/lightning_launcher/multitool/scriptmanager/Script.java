package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by Lukas on 22.08.2015.
 * Represents a script
 */
public class Script extends AbstractSectionableItem<Script.ViewHolder, ScriptGroup> implements Comparable<Script>, ScriptItem<Script.ViewHolder> {
    private int id;
    private String name;
    private String code;
    private int flags;

    public Script() {
        super(null);
        setSelectable(true);
        setDraggable(true);
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

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public int getFlags() {
        return flags;
    }

    public void fillFrom(Script script) {
        setName(script.getName());
        setId(script.getId());
        setCode(script.getCode());
        setFlags(script.getFlags());
    }

    @Override
    public int getLayoutRes() {
        return android.R.layout.simple_list_item_1;
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

    public static class ViewHolder extends FlexibleViewHolder {
        private final TextView textView;

        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            textView = (TextView) view.findViewById(android.R.id.text1);
        }

        public TextView getTextView() {
            return textView;
        }

        @Override
        public boolean isDraggable() {
            return super.isDraggable();
        }
    }
}
