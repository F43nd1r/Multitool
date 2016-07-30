package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * Created on 29.07.2016.
 *
 * @author F43nd1r
 */

public class ScriptGroupLayoutItem extends AbstractExpandableHeaderItem<ScriptGroupLayoutItem.ViewHolder, ScriptLayoutItem> {
    private final ScriptGroup group;

    public ScriptGroupLayoutItem(ScriptGroup group) {
        super();
        this.group = group;
    }

    @Override
    public int getLayoutRes() {
        return android.R.layout.simple_expandable_list_item_1;
    }

    @Override
    public boolean equals(Object o) {
        return group.equals(o);
    }

    @Override
    public ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
        holder.getTextView().setText(group.getName());
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
