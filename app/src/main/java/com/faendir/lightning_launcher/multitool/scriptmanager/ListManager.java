package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ViewGroup;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem;
import com.faendir.lightning_launcher.multitool.fastadapter.ItemFactory;
import com.faendir.lightning_launcher.multitool.fastadapter.Model;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.mikepenz.fastadapter.expandable.ExpandableExtension;
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import java9.lang.Iterables;
import java9.util.Optional;
import java9.util.function.Consumer;
import java9.util.stream.Collectors;
import java9.util.stream.IntStream;
import java9.util.stream.StreamSupport;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
class ListManager {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ModelAdapter<Model, ExpandableItem<Model>> adapter;
    private final FastAdapter<ExpandableItem<Model>> fastAdapter;
    private final ItemFactory<Model> factory;
    private final Consumer<Boolean> actionModeEnabler;
    private final ExpandableExtension<ExpandableItem<Model>> expandable;

    ListManager(@NonNull ScriptManager scriptManager, @NonNull Context context, Consumer<Boolean> actionModeEnabler) {
        this.context = context;
        recyclerView = new RecyclerView(context);
        factory = new ItemFactory<>((int) (24 * context.getResources().getDisplayMetrics().density));
        this.actionModeEnabler = actionModeEnabler;
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        adapter = new ModelAdapter<>(factory::wrap);
        fastAdapter = FastAdapter.with(adapter);
        expandable = new ExpandableExtension<>();
        fastAdapter.addExtension(expandable);
        fastAdapter.withSelectable(true)
                .withMultiSelect(true)
                .withSelectWithItemUpdate(true)
                .withSelectionListener((item, selected) -> fireUpdateActionMode());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(fastAdapter);
        new ItemTouchHelper(new SimpleSwipeCallback((position, direction) -> {
            final ExpandableItem<Model> item = adapter.getAdapterItem(position);
            final Runnable removeRunnable = () -> {
                item.setSwipedAction(null);
                int position1 = adapter.getAdapterPosition(item);
                if (position1 != RecyclerView.NO_POSITION) {
                    adapter.remove(position1);
                }
                ScriptUtils.deleteScript(scriptManager, this, (Script) item.getModel());
            };
            recyclerView.postDelayed(removeRunnable, 5000);

            item.setSwipedAction(() -> {
                recyclerView.removeCallbacks(removeRunnable);
                item.setSwipedAction(null);
                int position2 = adapter.getAdapterPosition(item);
                if (position2 != RecyclerView.NO_POSITION) {
                    fastAdapter.notifyAdapterItemChanged(position2);
                }
            });

            fastAdapter.notifyAdapterItemChanged(position);
        }, null, ItemTouchHelper.RIGHT).withLeaveBehindSwipeRight(context.getResources().getDrawable(R.drawable.ic_delete_white)).withBackgroundSwipeRight(Color.RED))
                .attachToRecyclerView(recyclerView);
    }


    private void fireUpdateActionMode() {
        actionModeEnabler.accept(!fastAdapter.getSelections().isEmpty());
    }

    void deselectAll() {
        fastAdapter.deselect();
    }

    void changed(Model s) {
    }

    void updateFrom(@NonNull List<Script> scripts) {
        List<ExpandableItem<Model>> items = new ArrayList<>();
        for (Script script : scripts) {
            String path = script.getPath();
            List<ExpandableItem<Model>> parentItems = items;
            ExpandableItem<Model> parent = null;
            if (path != null) {
                String[] pathFolders = path.split("/");
                for (String folder : pathFolders) {
                    if ("".equals(folder)) {
                        continue;
                    }
                    Optional<ExpandableItem<Model>> optional = StreamSupport.stream(parentItems)
                            .filter(item -> item.getModel() instanceof Folder).filter(item -> item.getModel().getName().equals(folder))
                            .findAny();
                    if (optional.isPresent()) {
                        parent = optional.get();
                    } else {
                        ExpandableItem<Model> f = factory.wrap(new Folder(folder));
                        parentItems.add(f);
                        if (parent != null) {
                            f.withParent(parent);
                        }
                        f.withSubItems(new ArrayList<>());
                        parent = f;
                    }
                    parentItems = parent.getSubItems();
                }
            }
            ExpandableItem<Model> s = factory.wrap(script);
            parentItems.add(s);
            if (parent != null) {
                s.withParent(parent);
            }
        }
        Queue<ExpandableItem<Model>> queue = new ArrayDeque<>(items);
        while (queue.peek() != null) {
            ExpandableItem<Model> item = queue.remove();
            if (item.getModel() instanceof Folder) {
                while (item.getSubItems().size() == 1) {
                    ExpandableItem<Model> child = item.getSubItems().get(0);
                    if (child.getModel() instanceof Folder) {
                        item.withSubItems(child.getSubItems());
                        ((Folder) item.getModel()).setName(item.getModel().getName() + "/" + child.getModel().getName());
                    } else {
                        break;
                    }
                }
                queue.addAll(item.getSubItems());
            }
        }
        int[] expanded = expandable.getExpandedItems();
        IntStream.Builder builder = IntStream.builder();
        int start = 0;
        for (int e : expanded) {
            for (int i = start; i < e; i++) {
                builder.accept(i);
            }
            start = e + 1;
        }
        for (int i = start; i < adapter.getAdapterItemCount(); i++) {
            builder.accept(i);
        }
        List<Model> collapsedItems = builder.build().mapToObj(adapter::getAdapterItem).map(ExpandableItem::getModel).collect(Collectors.toList());
        new Handler(context.getMainLooper()).post(() -> {
            adapter.setInternal(items, false, null);
            Iterables.forEach(items, item -> ListManager.this.recursiveExpand(item, collapsedItems));
        });
    }

    private void recursiveExpand(ExpandableItem<Model> item, List<Model> exclude) {
        if (StreamSupport.stream(exclude).noneMatch(item.getModel()::equals)) {
            expandable.expand(adapter.getAdapterPosition(item));
        }
        if (item.getSubItems() != null) {
            for (ExpandableItem<Model> i : item.getSubItems()) {
                recursiveExpand(i, exclude);
            }
        }
    }

    void setAsContentOf(final ViewGroup group) {
        new Handler(context.getMainLooper()).post(() -> {
            group.removeAllViews();
            group.addView(recyclerView);
        });
    }

    List<Model> getItems() {
        return adapter.getModels();
    }

    boolean exists(Script script) {
        return exists(script, getItems());
    }

    private boolean exists(Script script, List<Model> searchIn) {
        return StreamSupport.stream(searchIn).filter(item -> script.getName().equals(item.getName())).findAny().isPresent();
    }

    @IntDef({NONE, ONE_SCRIPT, ONE_GROUP, ONLY_GROUPS, ONLY_SCRIPTS, BOTH})
    @interface SelectionMode {
    }

    static final int NONE = -1;
    static final int ONE_SCRIPT = 0;
    static final int ONE_GROUP = 1;
    static final int ONLY_SCRIPTS = 2;
    private static final int ONLY_GROUPS = 3;
    private static final int BOTH = 4;

    @SelectionMode
    int getSelectionMode() {
        Map<Boolean, List<Model>> m = StreamSupport.stream(fastAdapter.getSelectedItems()).map(ExpandableItem::getModel).collect(Collectors.partitioningBy(Folder.class::isInstance));
        List<Model> selectedScriptGroups = m.get(true);
        List<Model> selectedScripts = m.get(false);
        boolean noScripts = selectedScripts.isEmpty();
        return selectedScriptGroups.isEmpty() ? noScripts ? NONE
                : selectedScripts.size() == 1 ? ONE_SCRIPT : ONLY_SCRIPTS
                : selectedScriptGroups.size() == 1 ? noScripts ? ONE_GROUP : BOTH
                : noScripts ? ONLY_GROUPS : BOTH;
    }

    List<Model> getSelectedItems() {
        return StreamSupport.stream(fastAdapter.getSelectedItems()).map(ExpandableItem::getModel).collect(Collectors.toList());
    }
}
