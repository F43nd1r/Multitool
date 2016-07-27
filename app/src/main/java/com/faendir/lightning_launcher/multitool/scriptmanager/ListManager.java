package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.ToStringBuilder;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
class ListManager {

    @NonNull
    private final ScriptManager scriptManager;
    private final Context context;
    private final ExpandableListView listView;
    private ScriptListAdapter adapter;
    private List<ScriptGroup> items;
    private Parcelable listViewState;

    public ListManager(@NonNull ScriptManager scriptManager, @NonNull Context context, final ClickListener listener) {
        this.scriptManager = scriptManager;
        this.context = context;
        listView = new ExpandableListView(context);
        listView.setDrawSelectorOnTop(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    return listener.onChildLongClick(id);
                } else {
                    return listener.onGroupLongClick(id);
                }
            }
        });
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return listener.onChildClick(id);
            }
        });
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return listener.onGroupClick(id);
            }
        });
    }

    public void toggleItem(long packedPos) {
        selectItem(packedPos, !isSelected(packedPos));
    }

    private void selectItem(long packedPos, boolean select) {
        adapter.select(packedPos, select);
        adapter.notifyDataSetChanged();
    }

    public boolean hasSelection() {
        return adapter.getSelectedPackedPosition().size() > 0;
    }

    public boolean isSelected(long packedPos) {
        return adapter.isSelected(packedPos);
    }

    public void deselectChildren(long packedGroupPos) {
        int groupPosition = listView.getFlatListPosition(packedGroupPos);
        if (listView.isGroupExpanded(groupPosition)) {
            for (int i = adapter.getChildrenCount(groupPosition) - 1; i >= 0; i--) {
                selectItem(ExpandableListView.getPackedPositionForChild(groupPosition, i), false);
            }
        }
    }

    public void deselectAll() {
        adapter.deselectAll();
        adapter.notifyDataSetChanged();
    }

    public void delete(final List<ScriptItem> delete) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ScriptItem item : delete) {
                    loop:
                    for (ScriptGroup group : items) {
                        if (group.equals(item)) {
                            if (prepareGroupForDelete(group)) {
                                items.remove(group);
                            }
                            break;
                        } else {
                            for (Script script : group) {
                                if (script.equals(item)) {
                                    ScriptUtils.deleteScript(scriptManager, ListManager.this, script);
                                    group.remove(script);
                                    break loop;
                                }
                            }
                        }
                    }
                }
            }
        }).start();
    }

    public void move(List<ScriptItem> selectedItems, ScriptGroup moveTo) {
        for (ScriptItem item : selectedItems) {
            if (item instanceof Script) {
                loop:
                for (ScriptGroup group : items) {
                    for (Script script : group) {
                        if (script.equals(item)) {
                            moveTo.add(script);
                            group.remove(script);
                            break loop;
                        }
                    }
                }
            }
        }
    }

    private boolean prepareGroupForDelete(ScriptGroup delete) {
        if (!delete.allowsDelete()) return false;
        ScriptGroup def = null;
        for (ScriptGroup s : items) {
            if (!s.allowsDelete()) {
                def = s;
            }
        }
        assert def != null;
        for (Script item : delete) {
            def.add(item);
        }
        return true;
    }

    public void updateFrom(@NonNull List<Script> scripts) {
        ArrayList<Script> existing = new ArrayList<>();
        if (items == null) {
            items = new ArrayList<>();
            ScriptGroup def = new ScriptGroup(context.getString(R.string.text_defaultScriptGroup), false);
            items.add(def);
            for (Script s : scripts) {
                def.add(s);
            }
        }
        ScriptGroup def = null;
        for (ScriptGroup i : items) {
            for (Iterator<Script> it = i.iterator(); it.hasNext(); ) {
                Script s = it.next();
                if (!checkIfStillExisting(scripts, s)) it.remove();
                else existing.add(s);
            }
            if (!i.allowsDelete()) def = i;
        }
        assert def != null;
        for (Script s : scripts) {
            if (!existing.contains(s)) {
                def.add(s);
            }
        }
        adapter = new ScriptListAdapter(context, items, listView);
        listView.setAdapter(adapter);
    }

    private boolean checkIfStillExisting(List<Script> scripts, Script item) {
        if (scripts.contains(item)) {
            Script script = scripts.get(scripts.indexOf(item));
            item.fillFrom(script);
        } else {
            return false;
        }
        return true;
    }

    public void restoreFrom(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(context.getString(R.string.key_listView))) {
            listView.onRestoreInstanceState(savedInstanceState.getParcelable(context.getString(R.string.key_listView)));
        }
    }

    public void saveTo(@NonNull Bundle bundle) {
        bundle.putParcelable(context.getString(R.string.key_listView), listView.onSaveInstanceState());
    }

    public void restoreFrom(FileManager<ScriptGroup> fileManager) {
        items = fileManager.read();
    }

    public void saveTo(FileManager<ScriptGroup> fileManager) {
        if (items != null) fileManager.write(items);
    }

    public void restore() {
        if (listViewState != null) listView.onRestoreInstanceState(listViewState);
    }

    public void save() {
        listViewState = listView.onSaveInstanceState();
    }

    public void setAsContentOf(final ViewGroup group) {
        new Handler(context.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        group.removeAllViews();
                        group.addView(listView);
                    }
                });
    }

    public void createGroup(String name) {
        items.add(new ScriptGroup(name, true));
        adapter.notifyDataSetChanged();
    }

    public List<ScriptGroup> getItems() {
        return items;
    }

    public boolean exists(Script script) {
        for (ScriptGroup group : items) {
            for (Script s : group) {
                if (s.equals(script)) {
                    return true;
                }
            }
        }
        return false;
    }

    @IntDef({NONE, ONE_SCRIPT, ONE_GROUP, ONLY_GROUPS, ONLY_SCRIPTS, BOTH})
    @interface SelectionMode {
    }

    public static final int NONE = -1;
    public static final int ONE_SCRIPT = 0;
    public static final int ONE_GROUP = 1;
    public static final int ONLY_SCRIPTS = 2;
    private static final int ONLY_GROUPS = 3;
    private static final int BOTH = 4;

    @SelectionMode
    public int getSelectionMode() {
        int selectionMode = NONE;
        List<Long> checked = adapter.getSelectedPackedPosition();
        if (checked.size() == 1) {
            int type = ExpandableListView.getPackedPositionType(checked.get(0));
            selectionMode = type == ExpandableListView.PACKED_POSITION_TYPE_GROUP ? ONE_GROUP : ONE_SCRIPT;
        } else {
            for (long position : checked) {
                int type = ExpandableListView.getPackedPositionType(position);
                boolean isGroup = type == ExpandableListView.PACKED_POSITION_TYPE_GROUP;
                if (selectionMode == NONE) {
                    selectionMode = isGroup ? ONLY_GROUPS : ONLY_SCRIPTS;
                } else if ((selectionMode == ONLY_GROUPS && !isGroup) || (selectionMode == ONLY_SCRIPTS && isGroup)) {
                    selectionMode = BOTH;
                    break;
                }
            }
        }
        return selectionMode;
    }

    public List<ScriptItem> getSelectedItems() {
        List<Long> selection = adapter.getSelectedPackedPosition();
        List<ScriptItem> selectedItems = new ArrayList<>();
        for (Long l : selection) {
            int type = ExpandableListView.getPackedPositionType(l);
            if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                selectedItems.add(items.get(ExpandableListView.getPackedPositionGroup(l)));
            } else {
                selectedItems.add(items.get(ExpandableListView.getPackedPositionGroup(l)).get(ExpandableListView.getPackedPositionChild(l)));
            }
        }
        return selectedItems;
    }

    public interface ClickListener {
        boolean onChildClick(long packedPos);

        boolean onChildLongClick(long packedPos);

        boolean onGroupClick(long packedPos);

        boolean onGroupLongClick(long packedPos);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("adapter", adapter).append("listViewState", listViewState).build();
    }
}
