package com.faendir.lightning_launcher.multitool.badge

import android.app.Activity
import android.content.Intent
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.proxy.*

/**
 * @author lukas
 * @since 08.07.18
 */
@Keep
class BadgeSetup(private val utils: Utils) : JavaScript.Setup, JavaScript.ActivityResult {

    override fun setup() {
        val create = utils.installActivityResultScript()
        val screen = ProxyFactory.cast(utils.activeScreen, ActivityScreen::class.java)
        val d = utils.container
        val intent = Intent(utils.multitoolContext, AppChooser::class.java)
        screen.startActivityForResult(intent, create, javaClass.name + "/" + d.id)
    }

    override fun onActivityResult(resultCode: Int, data: Intent, token: String) {
        if (resultCode == Activity.RESULT_OK) {
            val d = utils.activeScreen.getContainerById(Integer.parseInt(token))
            val resume = utils.installRegisterScript()
            val pause = utils.installUnregisterScript()
            val item = d.addShortcut("0", Intent(), 0f, 0f)
            val intent = data.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

            item.setTag(TAG_PACKAGE, intent.component?.packageName)
            item.properties
                    .edit()
                    .setBoolean(PropertySet.ITEM_ON_GRID, false)
                    .setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false)
                    .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, true)
                    .setBoolean(PropertySet.ITEM_ENABLED, false)
                    .setEventHandler(PropertySet.ITEM_RESUMED, EventHandler.RUN_SCRIPT, resume.id.toString() + "/" + BadgeListener::class.java.name)
                    .setEventHandler(PropertySet.ITEM_PAUSED, EventHandler.RUN_SCRIPT, pause.id.toString())
                    .commit()
            utils.centerOnTouch(item)
        }
    }

    companion object {
        internal const val TAG_PACKAGE = "package"
    }
}
