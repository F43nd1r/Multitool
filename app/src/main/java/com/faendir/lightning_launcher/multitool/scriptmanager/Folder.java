package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.Model;

/**
 * @author F43nd1r
 * @since 29.10.2016
 */

public class Folder implements Comparable<Folder>, Model {

    private String name;

    public Folder(@NonNull String name) {
        this.name = name;
    }

    @Override
    public int compareTo(@NonNull Folder o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Drawable getIcon(@NonNull Context context) {
        return context.getResources().getDrawable(R.drawable.ic_folder_white);
    }

    @Override
    public int getTintColor() {
        return Color.WHITE;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Folder folder = (Folder) o;

        return name.equals(folder.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
