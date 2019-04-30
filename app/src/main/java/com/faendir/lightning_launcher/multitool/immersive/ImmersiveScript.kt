package com.faendir.lightning_launcher.multitool.immersive

import android.app.Activity
import android.os.Handler
import android.view.View
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.proxy.EventHandler
import com.faendir.lightning_launcher.multitool.proxy.JavaScript
import com.faendir.lightning_launcher.multitool.proxy.PropertySet
import com.faendir.lightning_launcher.multitool.proxy.Utils

/**
 * @author lukas
 * @since 09.07.18
 */
@Keep
class ImmersiveScript(private val utils: Utils) : JavaScript.Normal, JavaScript.Setup {

    override fun setup() {
        val script = utils.installNormalScript()
        val desktop = utils.activeScreen.currentDesktop
        val properties = desktop.properties
        val eventHandler = properties.getEventHandler(PropertySet.RESUMED)
        if (eventHandler?.action == EventHandler.RUN_SCRIPT && eventHandler?.data?.startsWith(script.id.toString()) == true) {
            properties.edit().setEventHandler(PropertySet.RESUMED, EventHandler.UNSET, null).commit()
            (utils.lightningContext as? Activity)?.window?.decorView?.systemUiVisibility = 0
        } else {
            properties.edit().setEventHandler(PropertySet.RESUMED, EventHandler.RUN_SCRIPT, script.id.toString() + "/" + javaClass.name).commit()
            script.run(utils.activeScreen, ImmersiveScript::class.java.name)
        }
    }

    override fun run() {
        Handler(utils.lightningContext.mainLooper).post {
            (utils.lightningContext as? Activity)?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }
}
