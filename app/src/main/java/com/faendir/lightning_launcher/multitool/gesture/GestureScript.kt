package com.faendir.lightning_launcher.multitool.gesture

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.MainActivity
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.*
import com.faendir.lightning_launcher.multitool.util.FragmentManager

/**
 * @author lukas
 * @since 09.07.18
 */
@Keep
class GestureScript(private val utils: Utils) : JavaScript.CreateMenu, JavaScript.Setup, JavaScript.CreateCustomView {

    override fun showMenu(menu: Menu, item: Item) {
        val mode = menu.mode
        if (mode == Menu.MODE_ITEM_NO_EM || mode == Menu.MODE_ITEM_EM) {
            menu.addMainItem(utils.getString(R.string.menu_editGestures), utils.asFunction(Runnable {
                val intent = Intent(utils.multitoolContext, MainActivity::class.java)
                intent.putExtra(FragmentManager.EXTRA_MODE, R.string.title_gestureLauncher)
                utils.lightningContext.startActivity(intent)
                menu.close()
            }))
        }
    }

    override fun setup() {
        val screen = utils.activeScreen
        val view = utils.container.addCustomView(screen.lastTouchX, screen.lastTouchY)
        view.setHorizontalGrab(true)
        view.setVerticalGrab(true)
        val script = utils.installCreateViewScript()
        val menu = utils.installMenuScript()
        view.properties
                .edit()
                .setString(PropertySet.VIEW_ON_CREATE, script.id.toString() + "/" + javaClass.name)
                .setString(PropertySet.ITEM_SELECTION_EFFECT, PropertySet.ITEM_SELECTION_EFFECT_PLAIN)
                .setEventHandler(PropertySet.ITEM_MENU, EventHandler.RUN_SCRIPT, menu.id.toString() + "/" + javaClass.name)
                .also { it.getBox(PropertySet.ITEM_BOX).setColor(Box.CONTENT, Box.MODE_ALL, 0x42FfFfFf) }
                .commit()
    }

    override fun onCreate(item: CustomView): View {
        item.setHorizontalGrab(true)
        item.setVerticalGrab(true)

        return try {
            LightningGestureView(utils)
        } catch (e: Exception) {
            Log.w(MultiTool.LOG_TAG, "Failed to load gesture widget")
            val t = TextView(utils.lightningContext)
            t.text = utils.getString(R.string.text_gestureViewFailed)
            t
        }

    }
}
