package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.UpdateActionModeRequest;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.ToStringBuilder;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
class ListManager implements ActionMode.Callback {

    @NonNull
    private final ScriptManager scriptManager;
    private final Context context;
    private final RecyclerView recyclerView;
    private List<ScriptGroup> items;
    private FlexibleAdapter<ScriptItem> flexibleAdapter;

    public ListManager(@NonNull ScriptManager scriptManager, @NonNull final Context context) {
        this.scriptManager = scriptManager;
        this.context = context;
        FlexibleAdapter.enableLogs(true);
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(context));
        flexibleAdapter = new FlexibleAdapter<>(new ArrayList<ScriptItem>());
        items = new DoubleBackedList(new ArrayList<ScriptGroup>(), flexibleAdapter);
        flexibleAdapter
                .initializeListeners(new FlexibleAdapter.OnItemClickListener() {
                    @Override
                    public boolean onItemClick(int position) {
                        if (!(flexibleAdapter.getItem(position) instanceof ScriptGroup)) {
                            flexibleAdapter.toggleSelection(position);
                            updateActionMode();
                        }
                        return true;
                    }
                })
                .initializeListeners(new FlexibleAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(int position) {
                        flexibleAdapter.toggleSelection(position);
                        updateActionMode();
                    }
                })
//                .expandItemsAtStartUp()
                .setAutoScrollOnExpand(true)
                .setAutoCollapseOnExpand(false)
                .setAnimationOnScrolling(true)
                .setAnimationOnReverseScrolling(true)
                .setMode(FlexibleAdapter.MODE_MULTI);
        recyclerView.setAdapter(flexibleAdapter);
        flexibleAdapter.setLongPressDragEnabled(true);
    }

    private void updateActionMode() {
        EventBus.getDefault().post(new UpdateActionModeRequest(ListManager.this, flexibleAdapter.getSelectedItemCount() > 0));
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
                            flexibleAdapter.notifyItemChanged(flexibleAdapter.getGlobalPositionOf(moveTo));
                            flexibleAdapter.notifyItemChanged(flexibleAdapter.getGlobalPositionOf(group));
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
        if (items.isEmpty()) {
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
        if (savedInstanceState != null) {
            flexibleAdapter.onRestoreInstanceState(savedInstanceState);
        }
    }

    public void saveTo(@NonNull Bundle bundle) {
        flexibleAdapter.onSaveInstanceState(bundle);
    }

    public void restoreFrom(FileManager<ScriptGroup> fileManager) {
        List<ScriptGroup> i = fileManager.read();
        if (i != null) {
            items.clear();
            items.addAll(i);
        }
    }

    public void saveTo(FileManager<ScriptGroup> fileManager) {
        if (items != null) fileManager.write(items);
    }

    public void setAsContentOf(final ViewGroup group) {
        new Handler(context.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        group.removeAllViews();
                        group.addView(recyclerView);
                    }
                });
    }

    public void createGroup(String name) {
        items.add(new ScriptGroup(name, true));
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

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_context_scriptmanager, menu);
        onPrepareActionMode(mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int selectionMode = getSelectionMode();
        if (selectionMode == ListManager.NONE) mode.finish();
        menu.findItem(R.id.action_rename).setVisible(selectionMode == ListManager.ONE_GROUP || selectionMode == ListManager.ONE_SCRIPT);
        menu.findItem(R.id.action_delete).setVisible(true);
        menu.findItem(R.id.action_move_to_group).setVisible(selectionMode == ListManager.ONLY_SCRIPTS || selectionMode == ListManager.ONE_SCRIPT);
        menu.findItem(R.id.action_edit).setVisible(selectionMode == ListManager.ONE_SCRIPT);
        menu.findItem(R.id.action_backup).setVisible(selectionMode == ListManager.ONLY_SCRIPTS || selectionMode == ListManager.ONE_SCRIPT);
        menu.findItem(R.id.action_format).setVisible(selectionMode == ListManager.ONLY_SCRIPTS || selectionMode == ListManager.ONE_SCRIPT);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<ScriptItem> selectedItems = getSelectedItems();
        if (selectedItems.isEmpty()) {
            ACRA.getErrorReporter().putCustomData("listManager", toString());
            ACRA.getErrorReporter().handleSilentException(new IllegalStateException("No selected items"));
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_rename:
                ScriptUtils.renameDialog(scriptManager, context, this, selectedItems.get(0));
                break;
            case R.id.action_delete:
                ScriptUtils.deleteDialog(context, this, selectedItems);
                break;
            case R.id.action_move_to_group:
                ScriptUtils.moveDialog(context, this, selectedItems);
                break;
            case R.id.action_edit:
                ScriptUtils.editScript(context, this, (Script) selectedItems.get(0));
                break;
            case R.id.action_backup:
                ScriptUtils.backup(context, this, selectedItems);
                break;
            case R.id.action_format:
                ScriptUtils.format(scriptManager, context, this, selectedItems);
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        flexibleAdapter.clearSelection();
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
        int selectionMode;
        boolean childSelected = flexibleAdapter.isAnyChildSelected();
        boolean parentSelected = flexibleAdapter.isAnyParentSelected();
        boolean onlyOne = flexibleAdapter.getSelectedItemCount() == 1;
        selectionMode = childSelected ? parentSelected ? BOTH : onlyOne ? ONE_SCRIPT : ONLY_SCRIPTS : parentSelected ? onlyOne ? ONE_GROUP : ONLY_GROUPS : NONE;
        return selectionMode;
    }

    public List<ScriptItem> getSelectedItems() {
        List<Integer> selection = flexibleAdapter.getSelectedPositions();
        List<ScriptItem> selectedItems = new ArrayList<>();
        for (Integer position : selection) {
            selectedItems.add(flexibleAdapter.getItem(position));
        }
        return selectedItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("adapter", flexibleAdapter).build();
    }
}
