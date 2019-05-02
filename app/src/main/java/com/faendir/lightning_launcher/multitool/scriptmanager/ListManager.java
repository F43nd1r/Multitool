package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem;
import com.faendir.lightning_launcher.multitool.fastadapter.ItemFactory;
import com.faendir.lightning_launcher.multitool.fastadapter.Model;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.mikepenz.fastadapter.expandable.ExpandableExtension;
import com.mikepenz.fastadapter.select.SelectExtension;
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback;
import java9.lang.Iterables;
import java9.util.Optional;
import java9.util.function.Consumer;
import java9.util.stream.Collectors;
import java9.util.stream.IntStream;
import java9.util.stream.StreamSupport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
class ListManager {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ModelAdapter<Model, ExpandableItem<Model>> adapter;
    private final ItemFactory<Model> factory;
    private final ExpandableExtension<ExpandableItem<Model>> expandable;
    private final SelectExtension<ExpandableItem<Model>> selectable;

    ListManager(@NonNull Context context, Consumer<Boolean> actionModeEnabler) {
        this.context = context;
        recyclerView = new RecyclerView(context);
        factory = new ItemFactory<>((int) (24 * context.getResources().getDisplayMetrics().density));
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        adapter = new ModelAdapter<>(factory::wrap);
        expandable = new ExpandableExtension<>();
        selectable = new SelectExtension<>();
        selectable.withSelectable(true).withMultiSelect(true).withSelectWithItemUpdate(true)
                .withSelectionListener(((item, selected) -> actionModeEnabler.accept(!selectable.getSelections().isEmpty())));
        FastAdapter fastAdapter = FastAdapter.with(adapter);
        fastAdapter.addExtension(selectable).addExtension(expandable);
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
                ScriptUtils.deleteScript(this, (Script) item.getModel());
            };
            recyclerView.postDelayed(removeRunnable, 5000);

            item.setSwipedAction(() -> {
                recyclerView.removeCallbacks(removeRunnable);
                item.setSwipedAction(null);
                int position2 = adapter.getAdapterPosition(item);
                if (position2 != RecyclerView.NO_POSITION) {
                    fastAdapter.notifyAdapterItemChanged(position2);
                }
                return null;
            });

            fastAdapter.notifyAdapterItemChanged(position);
        }, null, ItemTouchHelper.RIGHT).withLeaveBehindSwipeRight(ContextCompat.getDrawable(context, R.drawable.ic_delete_white)).withBackgroundSwipeRight(Color.RED))
                .attachToRecyclerView(recyclerView);
    }

    void deselectAll() {
        selectable.deselect();
    }

    void update() {
        update(null);
    }

    void update(@Nullable Runnable onDone) {
        MultiTool.get().doInLL(scriptService -> {
            updateFrom(scriptService.getScriptsMatching(net.pierrox.lightning_launcher.api.Script.FLAG_ALL));
            if(onDone != null) {
                onDone.run();
            }
        });
    }

    private void updateFrom(@NonNull List<net.pierrox.lightning_launcher.api.Script> scripts) {
        List<ExpandableItem<Model>> items = new ArrayList<>();
        for (net.pierrox.lightning_launcher.api.Script script : scripts) {
            String path = script.getPath();
            List<ExpandableItem<Model>> parentItems = items;
            ExpandableItem<Model> parent = null;
            if (path != null) {
                String[] pathFolders = path.split("/");
                for (String folder : pathFolders) {
                    if ("".equals(folder)) {
                        continue;
                    }
                    Optional<ExpandableItem<Model>> optional = StreamSupport.stream(parentItems).filter(item -> item.getModel() instanceof Folder)
                            .filter(item -> item.getModel().getName().equals(folder)).findAny();
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
            ExpandableItem<Model> s = factory.wrap(new Script(script));
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
        return StreamSupport.stream(getItems()).filter(item -> script.getName().equals(item.getName())).findAny().isPresent();
    }

    List<Script> getSelectedItems() {
        return StreamSupport.stream(selectable.getSelectedItems()).map(ExpandableItem::getModel).map(Script.class::cast).collect(Collectors.toList());
    }
}
