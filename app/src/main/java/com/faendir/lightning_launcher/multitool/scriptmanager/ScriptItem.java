package com.faendir.lightning_launcher.multitool.scriptmanager;


import com.faendir.omniadapter.model.Component;

/**
 * Created by Lukas on 23.08.2015.
 * An item in the ExpandableListView
 */
interface ScriptItem extends Component {
    String getName();
    void setName(String name);
}
