package com.faendir.lightning_launcher.multitool.scriptmanager

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem
import com.faendir.lightning_launcher.multitool.fastadapter.ItemFactory
import com.faendir.lightning_launcher.multitool.fastadapter.Model
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ModelAdapter
import com.mikepenz.fastadapter.expandable.ExpandableExtension
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback
import java9.lang.Iterables
import java.util.*

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
internal class ListManager(private val context: Context, actionModeEnabler: (Boolean) -> Unit) {
    private val recyclerView: RecyclerView = RecyclerView(context)
    private val factory: ItemFactory<Model> = ItemFactory((24 * context.resources.displayMetrics.density).toInt())
    private val adapter: ModelAdapter<Model, ExpandableItem<Model>> = ModelAdapter { factory.wrap(it) }
    private val expandable: ExpandableExtension<ExpandableItem<Model>> = ExpandableExtension()
    private val selectable: SelectExtension<ExpandableItem<Model>> = SelectExtension()

    val items: List<Model>
        get() = adapter.models

    val selectedItems: List<Script>
        get() = selectable.selectedItems.map { it.model as Script }.toList()

    init {
        recyclerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        selectable.withSelectable(true).withMultiSelect(true).withSelectWithItemUpdate(true)
                .withSelectionListener { _, _ -> actionModeEnabler.invoke(selectable.selections.isNotEmpty()) }
        val fastAdapter = FastAdapter.with<ExpandableItem<Model>, ModelAdapter<Model, ExpandableItem<Model>>>(adapter)
        fastAdapter.addExtension(selectable).addExtension(expandable)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = fastAdapter
        ItemTouchHelper(SimpleSwipeCallback({ position, _ ->
            val item = adapter.getAdapterItem(position)
            val removeRunnable = {
                item.setSwipedAction(null)
                val position1 = adapter.getAdapterPosition(item)
                if (position1 != RecyclerView.NO_POSITION) {
                    adapter.remove(position1)
                }
                ScriptUtils.deleteScript(this, item.model as Script)
            }
            recyclerView.postDelayed(removeRunnable, 5000)
            item.setSwipedAction {
                recyclerView.removeCallbacks(removeRunnable)
                item.setSwipedAction(null)
                val position2 = adapter.getAdapterPosition(item)
                if (position2 != RecyclerView.NO_POSITION) {
                    fastAdapter.notifyAdapterItemChanged(position2)
                }
                null
            }
            fastAdapter.notifyAdapterItemChanged(position)
        }, null, ItemTouchHelper.RIGHT).withLeaveBehindSwipeRight(ContextCompat.getDrawable(context, R.drawable.ic_delete_white)).withBackgroundSwipeRight(Color.RED))
                .attachToRecyclerView(recyclerView)
    }

    fun deselectAll() = selectable.deselect()

    @JvmOverloads
    fun update(onDone: (() -> Unit)? = null) {
        MultiTool.get().doInLL { scriptService ->
            updateFrom(scriptService.getScriptsMatching(net.pierrox.lightning_launcher.api.Script.FLAG_ALL))
            onDone?.invoke()
        }
    }

    private fun updateFrom(scripts: List<net.pierrox.lightning_launcher.api.Script>) {
        val items = ArrayList<ExpandableItem<Model>>()
        for (script in scripts) {
            val path = script.path
            var parentItems: MutableList<ExpandableItem<Model>> = items
            var parent: ExpandableItem<Model>? = null
            if (path != null) {
                val pathFolders = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (folder in pathFolders) {
                    if ("" == folder) {
                        continue
                    }
                    parent = parentItems.filter { it.model is Folder }.firstOrNull { it.model.name == folder } ?: factory.wrap(Folder(folder)).also { f ->
                        parentItems.add(f)
                        if (parent != null) {
                            f.withParent(parent!!)
                        }
                        f.withSubItems(ArrayList())
                    }
                    parentItems = parent.subItems
                }
            }
            val s = factory.wrap(Script(script))
            parentItems.add(s)
            if (parent != null) {
                s.withParent(parent)
            }
        }
        val queue = ArrayDeque(items)
        while (queue.peek() != null) {
            val item = queue.remove()
            if (item.model is Folder) {
                while (item.subItems.size == 1) {
                    val child = item.subItems[0]
                    if (child.model is Folder) {
                        item.withSubItems(child.subItems)
                        (item.model as Folder).name = item.model.name + "/" + child.model.name
                    } else {
                        break
                    }
                }
                queue.addAll(item.subItems)
            }
        }
        val expanded = expandable.expandedItems
        val builder = mutableListOf<Int>()
        var start = 0
        for (e in expanded) {
            for (i in start until e) {
                builder.add(i)
            }
            start = e + 1
        }
        for (i in start until adapter.adapterItemCount) {
            builder.add(i)
        }
        val collapsedItems = builder.map { adapter.getAdapterItem(it) }.map({ it.model }).toList()
        Handler(context.mainLooper).post {
            adapter.setInternal(items, false, null)
            Iterables.forEach(items) { item -> this@ListManager.recursiveExpand(item, collapsedItems) }
        }
    }

    private fun recursiveExpand(item: ExpandableItem<Model>, exclude: List<Model>) {
        if (exclude.none { item.model == it }) {
            expandable.expand(adapter.getAdapterPosition(item))
        }
        if (item.subItems != null) {
            for (i in item.subItems) {
                recursiveExpand(i, exclude)
            }
        }
    }

    fun setAsContentOf(group: ViewGroup) {
        Handler(context.mainLooper).post {
            group.removeAllViews()
            group.addView(recyclerView)
        }
    }

    fun exists(script: Script): Boolean = items.filter { item -> script.name == item.name }.any()
}
