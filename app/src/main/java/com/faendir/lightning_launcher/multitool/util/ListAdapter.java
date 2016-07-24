package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lukas on 26.01.2016.
 */
public class ListAdapter<T extends Text> extends ArrayAdapter<T> {
    private static final int RESOURCE = R.layout.list_item_app;

    private final Context context;
    private final Set<Integer> selected;

    public ListAdapter(Context context, @NonNull List<T> list) {
        super(context, RESOURCE, list);
        this.context = context;
        selected = new HashSet<>();
    }

    public ListAdapter(Context context, T[] objects) {
        this(context, Arrays.asList(objects));
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewGroup v = (ViewGroup) convertView;
        if (convertView == null)
            v = (ViewGroup) LayoutInflater.from(context).inflate(RESOURCE, parent, false);
        Text item = getItem(position);
        if (item != null) {
            ((TextView) v.findViewById(R.id.txt)).setText(item.getText());
        }
        View img = v.findViewById(R.id.img);
        if (item instanceof ImageText) {
            ((ImageView) img).setImageDrawable(((ImageText) item).getImage(context));
        } else if (img != null) {
            v.removeView(img);
        }
        if (isSelected(position)) {
            v.setBackgroundColor(context.getResources().getColor(R.color.selector));
        } else {
            v.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }
        return v;
    }

    public void setSelection(int position, boolean selected) {
        if (selected) {
            this.selected.add(position);
        } else {
            this.selected.remove(position);
        }
    }

    public boolean isSelected(int position) {
        return selected.contains(position);
    }

    public void clearSelection() {
        selected.clear();
    }
}
