package com.faendir.lightning_launcher.multitool.scriptmanager

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.event.ClickEvent
import com.faendir.lightning_launcher.multitool.fastadapter.Model
import org.acra.ACRA
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * Created by Lukas on 22.08.2015.
 * Main activity of ScriptManager
 */
class ScriptManagerFragment : Fragment(), ActionMode.Callback {
    private lateinit var sharedPref: SharedPreferences
    private var enableMenu: Boolean = false
    private lateinit var layout: FrameLayout
    private lateinit var listManager: ListManager
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        listManager = ListManager(activity!!) { this.setActionModeEnabled(it) }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = FrameLayout(activity!!)
        return layout
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        loadFromLauncher()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = inflater.inflate(R.menu.menu_scriptmanager, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!enableMenu) {
            Toast.makeText(activity, R.string.toast_menuDisabled, Toast.LENGTH_SHORT).show()
            return true
        }
        when (item!!.itemId) {
            R.id.action_restore -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                startActivityForResult(intent, IMPORT)
            }
            R.id.action_search -> ScriptUtils.searchDialog(activity!!, listManager)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMPORT -> if (data != null && data.data != null) {
                    ScriptUtils.restoreFromFile(activity!!, listManager, data.data!!)
                }
                EXPORT -> if (data != null && data.data != null) {
                    ScriptUtils.backup(activity!!, listManager, listManager.selectedItems[0], data.data!!)
                }
            }
        }
    }

    private fun loadFromLauncher() {
        layout.removeAllViews()
        LayoutInflater.from(activity).inflate(R.layout.fragment_loading, layout)
        listManager.update {
            listManager.setAsContentOf(layout)
            enableMenu = true
        }
    }

    @Subscribe
    fun onReloadButton(event: ClickEvent) {
        if (event.id == R.id.button_reload) {
            sharedPref.edit().putInt(getString(R.string.pref_id), -1).apply()
            loadFromLauncher()
        }
    }

    private fun setActionModeEnabled(enable: Boolean) {
        if (enable) {
            if (actionMode == null) {
                actionMode = activity!!.startActionMode(this)
            } else {
                actionMode!!.invalidate()
            }
        } else if (actionMode != null) {
            actionMode!!.finish()
            actionMode = null
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.menu_context_scriptmanager, menu)
        onPrepareActionMode(mode, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        val selectionCount = listManager.selectedItems.size
        if (selectionCount == 0) {
            mode.finish()
        }
        val isOneScript = selectionCount == 1
        menu.findItem(R.id.action_rename).isVisible = isOneScript
        menu.findItem(R.id.action_edit).isVisible = isOneScript
        menu.findItem(R.id.action_backup).isVisible = isOneScript
        menu.findItem(R.id.action_format).isVisible = true
        val disable = menu.findItem(R.id.action_disable).setVisible(isOneScript)
        if (isOneScript) {
            disable.setTitle(if (listManager.selectedItems[0].isDisabled) R.string.menu_enable else R.string.menu_disable)
        }
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        val selectedItems = ArrayList<Model>(listManager.selectedItems)
        if (selectedItems.isEmpty()) {
            ACRA.getErrorReporter().putCustomData("listManager", listManager.toString())
            ACRA.getErrorReporter().handleSilentException(IllegalStateException("No selected items"))
            return false
        }
        when (item.itemId) {
            R.id.action_rename -> ScriptUtils.renameDialog(activity!!, listManager, selectedItems[0] as Script)
            R.id.action_edit -> ScriptUtils.editScript(activity!!, listManager, selectedItems[0] as Script)
            R.id.action_backup -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/javascript"
                val script = selectedItems[0] as Script
                intent.putExtra(Intent.EXTRA_TITLE, script.id.toString() + "_" + script.name.replace("[,\\./\\:*?\"<>\\|]", "_") + ".js")
                startActivityForResult(intent, EXPORT)
            }
            R.id.action_format -> ScriptUtils.format(activity!!, listManager, selectedItems)
            R.id.action_disable -> ScriptUtils.toggleDisable(listManager, selectedItems[0] as Script)
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) = listManager.deselectAll()

    companion object {
        private const val IMPORT = 1
        private const val EXPORT = 2
    }
}
