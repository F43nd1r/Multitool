package com.faendir.lightning_launcher.multitool.proxy

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.faendir.lightning_launcher.multitool.BuildConfig
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.util.LightningObjectFactory
import com.faendir.lightning_launcher.multitool.util.provider.RemoteSharedPreferences
import org.acra.util.StreamReader

/**
 * @author lukas
 * @since 05.07.18
 */
class Utils(eval: LightningObjectFactory.EvalFunction, private val functionFactory: LightningObjectFactory.FunctionFactory) {
    val lightning: Lightning = ProxyFactory.evalProxy(eval)
    val lightningContext: Context = lightning.activeScreen.context
    val multitoolContext: Context = lightningContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)

    val multitoolResources: Resources
        get() = multitoolContext.resources

    val event: Event
        get() = lightning.event

    val container: Container
        get() = event.container

    val activeScreen: Screen
        get() = lightning.activeScreen

    val imageClass: Image.Class
        get() = Image.Class.get(lightningContext)

    val sharedPref: SharedPreferences
        get() = RemoteSharedPreferences(lightningContext)

    fun getString(@StringRes res: Int): String {
        return multitoolContext.getString(res)
    }

    fun getString(@StringRes res: Int, vararg formatArgs: Any): String {
        return multitoolContext.getString(res, *formatArgs)
    }

    fun installNormalScript(): Script {
        return installScript(R.raw.normal, "run")
    }

    fun installMenuScript(): Script {
        return installScript(R.raw.menu, "menu")
    }

    fun installActivityResultScript(): Script {
        return installScript(R.raw.activity_result, "activity_result")
    }

    fun installCreateViewScript(): Script {
        return installScript(R.raw.create_view, "create_view")
    }

    fun installRegisterScript(): Script {
        return installScript(R.raw.register, "register")
    }

    fun installUnregisterScript(): Script {
        return installScript(R.raw.unregister, "unregister")
    }

    fun installCommandScript(): Script {
        return installScript(R.raw.command, "command")
    }

    private fun installScript(@RawRes res: Int, name: String): Script {
        val path = '/' + BuildConfig.APPLICATION_ID.replace('.', '/')
        var script = lightning.getScriptByPathAndName(path, name)
        val scriptText = StreamReader(multitoolResources.openRawResource(res)).read()
        if (script == null) {
            script = lightning.createScript(path, name, scriptText, 0)
        } else {
            script.text = scriptText
        }
        return script
    }

    fun centerOnTouch(item: Item) {
        val screen = lightning.activeScreen
        // use the last screen touch position, if any, as location for the new item
        var x = screen.lastTouchX
        var y = screen.lastTouchY
        val width = item.width.toFloat()
        val height = item.height.toFloat()
        if (x == Integer.MIN_VALUE.toFloat() || y == Integer.MIN_VALUE.toFloat()) {
            // no previous touch event, use a default position (can happen when using the hardware menu key for instance)
            x = width
            y = height
        } else {
            // center around the touch position
            x -= width / 2
            y -= height / 2
        }
        item.setPosition(x, y)
    }

    fun asFunction(consumer: (Any) -> Unit): Function = ProxyFactory.lightningProxy(functionFactory.asFunction(consumer), Function::class.java)

    fun addEventHandler(properties: PropertySet, @PropertySet.EventProperty key: String, action: Int, data: String) {
        val eventHandler = EventHandler.newInstance(lightningContext, action, data)
        val old = properties.getEventHandler(key)
        eventHandler.setNext(old)
        properties.edit().setEventHandler(key, eventHandler).commit()
    }
}
