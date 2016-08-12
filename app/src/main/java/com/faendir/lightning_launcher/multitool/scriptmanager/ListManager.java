package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.UpdateActionModeRequest;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.ToStringBuilder;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.omniadapter.Action;
import com.faendir.omniadapter.BaseOmniController;
import com.faendir.omniadapter.DeepObservableList;
import com.faendir.omniadapter.OmniAdapter;
import com.faendir.omniadapter.OmniBuilder;
import com.faendir.omniadapter.SelectionListener;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
class ListManager extends BaseOmniController<ScriptItem> implements ActionMode.Callback, SelectionListener<ScriptItem> {

    @NonNull
    private final ScriptManager scriptManager;
    private final Context context;
    private final RecyclerView listView;
    private OmniAdapter<ScriptItem> adapter;
    private DeepObservableList<ScriptGroup> items;

    public ListManager(@NonNull ScriptManager scriptManager, @NonNull Context context) {
        this.scriptManager = scriptManager;
        this.context = context;
        listView = new RecyclerView(context);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        items = new DeepObservableList<>();
        adapter = new OmniBuilder<>(context, items, this)
                .setClick(new Action.Click(Action.SELECT)
                        .setDefaultCompositeAction(Action.EXPAND))
                .setLongClick(new Action.LongClick(Action.DRAG)
                        .setDefaultCompositeAction(Action.SELECT))
                .setExpandUntilLevelOnStartup(1)
                .addSelectionListener(this)
                .build();
        listView.setAdapter(adapter);
    }

    private void fireUpdateActionMode() {
        EventBus.getDefault().post(new UpdateActionModeRequest(this, !adapter.getSelection().isEmpty()));
    }

    public void deselectAll() {
        adapter.clearSelection();
    }

    public void changed(ScriptItem s){
        adapter.notifyItemUpdated(s);
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
            i.getState().setExpanded(true);
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

    public void restoreFrom(FileManager<ScriptGroup> fileManager) {
        List<ScriptGroup> i = fileManager.read();
        if (i != null) {
            items.clear();
            items.addAll(i);
        }
        adapter.notifyDataSetChanged();
        fireUpdateActionMode();
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

    @Override
    public View createView(ViewGroup parent, int level) {
        return LayoutInflater.from(context).inflate(level == 0 ? R.layout.list_group : R.layout.list_item_script, parent, false);
    }

    @Override
    public void bindView(View view, ScriptItem item, int level) {
        ((TextView) view).setText(item.getName());
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
        deselectAll();
    }

    @Override
    public void onSelectionChanged(List<ScriptItem> selected) {
        fireUpdateActionMode();
    }

    @Override
    public void onSelectionCleared() {
        fireUpdateActionMode();
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
        //noinspection unchecked
        List<ScriptGroup> selectedScriptGroups = (List<ScriptGroup>) adapter.getSelectionByLevel(0);
        //noinspection unchecked
        List<Script> selectedScripts = (List<Script>) adapter.getSelectionByLevel(1);
        boolean noScripts = selectedScripts.isEmpty();
        return selectedScriptGroups.isEmpty() ? noScripts ? NONE
                : selectedScripts.size() == 1 ? ONE_SCRIPT : ONLY_SCRIPTS
                : selectedScriptGroups.size() == 1 ? noScripts ? ONE_GROUP : BOTH
                : noScripts ? ONLY_GROUPS : BOTH;
    }

    public List<ScriptItem> getSelectedItems() {
        //noinspection unchecked
        return (List<ScriptItem>) adapter.getSelection();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("adapter", adapter).build();
    }
}
