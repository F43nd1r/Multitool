package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.UpdateActionModeRequest;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.omniadapter.OmniAdapter;
import com.faendir.omniadapter.OmniBuilder;
import com.faendir.omniadapter.model.Action;
import com.faendir.omniadapter.model.ChangeInformation;
import com.faendir.omniadapter.model.DeepObservableList;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import java8.util.Optional;
import java8.util.stream.StreamSupport;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
class ListManager extends OmniAdapter.BaseExpandableController<Folder, ScriptItem> implements ActionMode.Callback, OmniAdapter.SelectionListener<ScriptItem>, OmniAdapter.UndoListener<ScriptItem> {

    @NonNull
    private final ScriptManager scriptManager;
    private final Context context;
    private final RecyclerView recyclerView;
    private final OmniAdapter<ScriptItem> adapter;
    private final DeepObservableList<ScriptItem> items;

    ListManager(@NonNull ScriptManager scriptManager, @NonNull Context context) {
        this.scriptManager = scriptManager;
        this.context = context;
        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        items = new DeepObservableList<>(ScriptItem.class);
        items.keepSorted((o1, o2) -> {
            if (o1 instanceof Folder) {
                if (o2 instanceof Folder) {
                    return ((Folder) o1).compareTo((Folder) o2);
                } else {
                    return -1;
                }
            } else if (o2 instanceof Folder) {
                return 1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        });
        adapter = new OmniBuilder<>(context, items, this)
                .setClick(new Action.Click(Action.SELECT)
                        .setDefaultCompositeAction(Action.EXPAND))
                .setSwipeToRight(new Action.Swipe(Action.REMOVE))
                .setExpandUntilLevelOnStartup(100)
                .addSelectionListener(this)
                .enableUndoForAction(Action.REMOVE, R.string.text_itemRemoved)
                .addUndoListener(this)
                .setInsetDpPerLevel(10)
                .attach(recyclerView);
    }

    private void fireUpdateActionMode() {
        EventBus.getDefault().post(new UpdateActionModeRequest(this, !adapter.getSelection().isEmpty()));
    }

    void deselectAll() {
        adapter.clearSelection();
    }

    void changed(ScriptItem s) {
        adapter.notifyItemUpdated(s);
    }

    void updateFrom(@NonNull List<Script> scripts) {
        items.beginBatchedUpdates();
        final List<Script> old = new ArrayList<>();
        items.visitDeep((scriptItem, i) -> {
            if (scriptItem instanceof Script) {
                old.add((Script) scriptItem);
            }
        }, false);
        List<Script> removed = new ArrayList<>(old);
        removed.removeAll(scripts);
        List<Script> added = new ArrayList<>(scripts);
        added.removeAll(old);
        for (Script script : added) {
            String path = script.getPath();
            String[] pathFolders = path.split("/");
            DeepObservableList<ScriptItem> parent = items;
            for (String folder : pathFolders) {
                if ("".equals(folder)) {
                    continue;
                }
                Optional<Folder> optional = StreamSupport.stream(parent)
                        .filter(item -> item instanceof Folder).map(Folder.class::cast).filter(item->item.getRealName().equals(folder))
                        .findAny();
                Folder f;
                if (optional.isPresent()) {
                    f = optional.get();
                } else {
                    f = new Folder(folder);
                    f.getState().setExpanded(true);
                    parent.add(f);
                }
                parent = f.getRealChildren();
            }
            parent.add(script);
        }
        for (Script script : removed) {
            String path = script.getPath();
            String[] pathFolders = path.split("/");
            List<Folder> folders = new ArrayList<>();
            DeepObservableList<ScriptItem> parent = items;
            for (String folder : pathFolders) {
                if ("".equals(folder)) continue;
                Optional<Folder> folderOptional = StreamSupport.stream(parent).filter(item -> item instanceof Folder)
                        .map(Folder.class::cast).filter(item -> item.getRealName().equals(folder)).findAny();
                if (folderOptional.isPresent()) {
                    folders.add(folderOptional.get());
                    parent = folderOptional.get().getRealChildren();
                }
            }
            if (!folders.isEmpty()) {
                folders.get(folders.size() - 1).getChildren().remove(script);
                ListIterator<Folder> iterator = folders.listIterator(folders.size());
                Folder lastEmpty = null;
                //noinspection StatementWithEmptyBody
                while (iterator.hasPrevious() && (lastEmpty = iterator.previous()).getRealChildren().size() == 0)
                    ;
                if (lastEmpty != null) {
                    List<ScriptItem> p = iterator.hasPrevious() ? iterator.previous().getRealChildren() : items;
                    p.remove(lastEmpty);
                }
            } else {
                items.remove(script);
            }
        }
        items.endBatchedUpdates();
    }

    void setAsContentOf(final ViewGroup group) {
        new Handler(context.getMainLooper()).post(() -> {
            group.removeAllViews();
            group.addView(recyclerView);
        });
    }

    DeepObservableList<ScriptItem> getItems() {
        return items;
    }

    boolean exists(Script script) {
        return exists(script, items);
    }

    private boolean exists(Script script, DeepObservableList<ScriptItem> searchIn) {
        return StreamSupport.stream(searchIn).filter(item -> script.getName().equals(item.getName())
                || item instanceof Folder && exists(script, ((Folder) item).getChildren())).findAny().isPresent();
    }

    @Override
    public View createView(ViewGroup parent, int level) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false);
    }

    @Override
    public void bindView(View view, ScriptItem item, int level) {
        AppCompatTextView txt = (AppCompatTextView) view;
        txt.setText(item.getName());
        boolean isScript = item instanceof Script;
        //noinspection deprecation
        Drawable icon = DrawableCompat.wrap(context.getResources().getDrawable(isScript ? R.drawable.ic_file_white : R.drawable.ic_folder_white));
        DrawableCompat.setTint(icon, isScript && ((Script) item).isDisabled() ? Color.RED : Color.WHITE);
        DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN);
        txt.setCompoundDrawablesWithIntrinsicBounds(icon.mutate(), null, null, null);
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
        final boolean isOneScript = selectionMode == ListManager.ONE_SCRIPT;
        final boolean onlyScripts = isOneScript || selectionMode == ListManager.ONLY_SCRIPTS;
        menu.findItem(R.id.action_rename).setVisible(selectionMode == ListManager.ONE_GROUP || isOneScript);
        menu.findItem(R.id.action_edit).setVisible(isOneScript);
        menu.findItem(R.id.action_backup).setVisible(onlyScripts);
        menu.findItem(R.id.action_format).setVisible(onlyScripts);
        MenuItem disable = menu.findItem(R.id.action_disable).setVisible(isOneScript);
        if (isOneScript) {
            disable.setTitle(((Script) getSelectedItems().get(0)).isDisabled() ? R.string.menu_enable : R.string.menu_disable);
        }
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
            case R.id.action_edit:
                ScriptUtils.editScript(context, this, (Script) selectedItems.get(0));
                break;
            case R.id.action_backup:
                ScriptUtils.backup(context, this, selectedItems);
                break;
            case R.id.action_format:
                ScriptUtils.format(scriptManager, context, this, selectedItems);
                break;
            case R.id.action_disable:
                ScriptUtils.toggleDisable(scriptManager, this, (Script) selectedItems.get(0));
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

    @Override
    public void onActionPersisted(List<? extends ChangeInformation<ScriptItem>> changes) {
        StreamSupport.stream(changes).filter(change -> change instanceof ChangeInformation.Remove && change.getComponent() instanceof Script)
                .map(change -> (Script) change.getComponent()).forEach(script -> ScriptUtils.deleteScript(scriptManager, ListManager.this, script));
    }

    @Override
    public void onActionReverted(List<? extends ChangeInformation<ScriptItem>> changes) {
    }

    @Override
    public boolean isSelectable(ScriptItem component) {
        return component instanceof Script;
    }

    @IntDef({NONE, ONE_SCRIPT, ONE_GROUP, ONLY_GROUPS, ONLY_SCRIPTS, BOTH})
    @interface SelectionMode {
    }

    private static final int NONE = -1;
    private static final int ONE_SCRIPT = 0;
    private static final int ONE_GROUP = 1;
    private static final int ONLY_SCRIPTS = 2;
    private static final int ONLY_GROUPS = 3;
    private static final int BOTH = 4;

    @SelectionMode
    private int getSelectionMode() {
        List<Folder> selectedScriptGroups = adapter.getSelectionByType(Folder.class);
        List<Script> selectedScripts = adapter.getSelectionByType(Script.class);
        boolean noScripts = selectedScripts.isEmpty();
        return selectedScriptGroups.isEmpty() ? noScripts ? NONE
                : selectedScripts.size() == 1 ? ONE_SCRIPT : ONLY_SCRIPTS
                : selectedScriptGroups.size() == 1 ? noScripts ? ONE_GROUP : BOTH
                : noScripts ? ONLY_GROUPS : BOTH;
    }

    private List<ScriptItem> getSelectedItems() {
        return adapter.getSelection();
    }
}
