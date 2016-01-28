package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lukas on 26.01.2016.
 */
public class ImageListAdapter extends ArrayAdapter<ImageText> {
    private static final int RESOURCE = R.layout.list_item_app;

    private final Context context;
    private final Set<Integer> selected;

    public <T extends ImageText> ImageListAdapter(Context context, List<T> list) {
        super(context, RESOURCE, list != null ? new ArrayList<ImageText>(list) : new ArrayList<ImageText>());
        this.context = context;
        selected = new HashSet<>();
    }

    public ImageListAdapter(Context context, ImageText[] objects) {
        this(context, Arrays.asList(objects));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (convertView == null) v = LayoutInflater.from(context).inflate(RESOURCE, parent, false);
        ImageText imageText = getItem(position);
        ((TextView) v.findViewById(R.id.txt)).setText(imageText.getText());
        ((ImageView) v.findViewById(R.id.img)).setImageDrawable(imageText.getImage(context));
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

    public boolean isSelected(int position){
        return selected.contains(position);
    }
}
