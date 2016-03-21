package com.faendir.lightning_launcher.multitool.viewcreator;

import com.faendir.lightning_launcher.multitool.util.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lukas on 20.03.2016.
 */
public class CustomView implements Text {
    private int width;
    private int height;
    private String name;
    private List<SubView> subViews;

    public CustomView() {
        subViews = new ArrayList<>();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public List<SubView> getSubViews() {
        return subViews;
    }

    public void addSubView(SubView view) {
        subViews.add(view);
    }

    @Override
    public String getText() {
        return getName();
    }
}
