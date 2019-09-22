package com.faendir.lightning_launcher.multitool.gesture

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem
import com.faendir.lightning_launcher.multitool.fastadapter.ItemFactory
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ModelAdapter
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class GestureFragment : Fragment() {
    private lateinit var adapter: ModelAdapter<GestureInfo, ExpandableItem<GestureInfo>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireActivity()
        val layout = LinearLayout(context)
        val recyclerView = RecyclerView(context)
        adapter = ModelAdapter { ItemFactory.forLauncherIconSize<GestureInfo>(context).wrap(it) }
        val fastAdapter = FastAdapter.with<ExpandableItem<GestureInfo>, ModelAdapter<GestureInfo, ExpandableItem<GestureInfo>>>(adapter)
        fastAdapter.withOnLongClickListener { _, adapter, item, _ ->
            val intent = Intent(context, GestureActivity::class.java)
            intent.putExtra(GestureActivity.GESTURE, item.model)
            intent.putExtra(INDEX, adapter.getAdapterPosition(item))
            startActivityForResult(intent, EDIT)
            true
        }
        adapter.set(GestureUtils.readFromFile(context))
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
                GestureUtils.delete(context, item.model, adapter.models)
            }
            recyclerView.postDelayed(removeRunnable, 5000)

            item.setSwipedAction {
                recyclerView.removeCallbacks(removeRunnable)
                item.setSwipedAction(null)
                val position2 = adapter.getAdapterPosition(item)
                if (position2 != RecyclerView.NO_POSITION) {
                    fastAdapter.notifyAdapterItemChanged(position2)
                }
            }

            fastAdapter.notifyAdapterItemChanged(position)
        }, null, ItemTouchHelper.RIGHT).withLeaveBehindSwipeRight(ContextCompat.getDrawable(context, R.drawable.ic_delete_white)).withBackgroundSwipeRight(Color.RED))
                .attachToRecyclerView(recyclerView)
        recyclerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val empty = inflater.inflate(R.layout.textview_empty_gestures_list, recyclerView, false) as TextView
        layout.addView(recyclerView)
        layout.addView(empty)
        return layout
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_gesture, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_gesture -> startActivityForResult(Intent(activity, GestureActivity::class.java), ADD)
            R.id.action_help -> AlertDialog.Builder(activity).setTitle(R.string.title_help).setMessage(R.string.message_helpGesture).setPositiveButton(R.string.button_ok, null)
                    .show()
            R.id.action_export -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = APPLICATION_ZIP
                intent.putExtra(Intent.EXTRA_TITLE, "Multitool_Gestures_" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + ".zip")
                startActivityForResult(intent, EXPORT)
            }
            R.id.action_import -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = APPLICATION_ZIP
                startActivityForResult(intent, IMPORT)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                ADD -> {
                    val gestureInfo = data.getParcelableExtra<GestureInfo>(GestureActivity.GESTURE)
                    adapter.add(gestureInfo)
                    activity?.let { GestureUtils.writeToFile(it, adapter.models) }
                }
                EDIT -> {
                    run {
                        val gestureInfo = data.getParcelableExtra<GestureInfo>(GestureActivity.GESTURE)
                        val position = data.getIntExtra(INDEX, -1)
                        if (position >= 0) {
                            adapter.set(position, gestureInfo)
                            activity?.let { GestureUtils.writeToFile(it, adapter.models) }
                        }
                    }
                    val uri = data.data
                    if (uri != null) {
                        activity?.let { GestureUtils.exportGestures(it, uri) }
                    }
                }
                EXPORT -> {
                    val uri = data.data
                    if (uri != null) {
                        activity?.let { GestureUtils.exportGestures(it, uri) }
                    }
                }
                IMPORT -> {
                    val uri = data.data
                    if (uri != null) {
                        activity?.let { GestureUtils.importGestures(it, uri, adapter.models) }
                    }
                }
                else -> super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    companion object {
        private const val ADD = 1
        private const val EDIT = 2
        private const val EXPORT = 3
        private const val IMPORT = 4
        private const val INDEX = "index"
        const val APPLICATION_ZIP = "application/zip"
    }
}
