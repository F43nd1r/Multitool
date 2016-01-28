package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lukas on 23.08.2015.
 * Adapter for the ExpandableLisrView which allows dataset modification
 */
class ScriptListAdapter extends BaseExpandableListAdapter {

    private static final int SELECTION_COLOR = R.color.selector;

    private static final int RESOURCE_GROUP = android.R.layout.simple_expandable_list_item_1;
    private static final int RESOURCE_CHILD = android.R.layout.simple_expandable_list_item_1;
    private static final Comparator<ScriptGroup> COMPARATOR = new Comparator<ScriptGroup>() {
        @Override
        public int compare(ScriptGroup lhs, ScriptGroup rhs) {
            return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
        }
    };

    private final Context context;
    private final List<ScriptGroup> items;
    private final ExpandableListView listView;
    private final Set<Long> checked;
    private final LayoutInflater inflater;

    public ScriptListAdapter(Context context, List<ScriptGroup> items, ExpandableListView listView) {
        super();
        this.context = context;
        this.items = items;
        this.listView = listView;
        this.checked = new HashSet<>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Collections.sort(items, COMPARATOR);
        listView.invalidateViews();
    }

    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return items.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return items.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return items.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = inflater.inflate(RESOURCE_GROUP, parent, false);
        } else {
            v = convertView;
        }
        if (groupPosition < getGroupCount()) {
            TextView text = (TextView) v.findViewById(android.R.id.text1);
            if (text != null) {
                text.setText(items.get(groupPosition).getName());
            }
        }
        if (checked.contains(ExpandableListView.getPackedPositionForGroup(groupPosition))) {
            v.setBackgroundColor(context.getResources().getColor(SELECTION_COLOR));
        } else {
            v.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (groupPosition >= getGroupCount() || childPosition >= getChildrenCount(groupPosition)) {
            return new View(context);
        }
        View v;
        if (convertView == null) {
            v = inflater.inflate(RESOURCE_CHILD, parent, false);
        } else {
            v = convertView;
        }
        if (groupPosition < getGroupCount() && childPosition < getChildrenCount(groupPosition)) {
            TextView text = (TextView) v.findViewById(android.R.id.text1);
            if (text != null) {
                text.setText(items.get(groupPosition).get(childPosition).getName());
            }
        }
        if (checked.contains(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition))) {
            v.setBackgroundColor(context.getResources().getColor(SELECTION_COLOR));
        } else {
            v.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void select(long packedPosition, boolean select) {
        if (select) checked.add(packedPosition);
        else checked.remove(packedPosition);
    }

    public boolean isSelected(long packedPosition) {
        return checked.contains(packedPosition);
    }

    public void deselectAll() {
        checked.clear();
    }

    public List<Long> getSelectedPackedPosition() {
        return Arrays.asList(checked.toArray(new Long[checked.size()]));
    }
}
