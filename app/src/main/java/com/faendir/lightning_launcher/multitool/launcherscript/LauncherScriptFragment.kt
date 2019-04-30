package com.faendir.lightning_launcher.multitool.launcherscript

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.event.ClickEvent
import com.faendir.lightning_launcher.multitool.util.Utils
import net.pierrox.lightning_launcher.api.Script
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class LauncherScriptFragment : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var importButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_launcher_script, container, false)
        nameTextView = v.findViewById(R.id.main_scriptName)
        importButton = v.findViewById(R.id.button_import)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
        nameTextView.text = sharedPrefs.getString(getString(R.string.preference_scriptName), getString(R.string.script_name))
        return v
    }

    override fun onPause() {
        super.onPause()
        saveName()
    }

    private fun saveName() {
        //save the name preference
        sharedPrefs.edit().putString(getString(R.string.preference_scriptName), nameTextView.text.toString()).apply()
    }


    @Subscribe
    fun onButtonClick(event: ClickEvent) {
        if (event.id == R.id.button_import) {
            saveName()
            importButton.text = getString(R.string.button_repositoryImporter_importing)
            MultiTool.get().doInLL { scriptService ->
                scriptService.updateScript(Script(Utils.readRawResource(activity!!, R.raw.multitool), nameTextView.text.toString(), activity!!.packageName, Script.FLAG_APP_MENU or Script.FLAG_ITEM_MENU))
                if (isAdded) {
                    activity?.runOnUiThread { importButton.text = getString(R.string.button_repositoryImporter_importOk) }
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


}
