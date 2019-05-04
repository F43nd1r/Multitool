package com.faendir.lightning_launcher.multitool.util

import android.util.Log
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.proxy.JavaScript
import com.faendir.lightning_launcher.multitool.proxy.Utils

/**
 * @author F43nd1r
 * @since 07.11.2017
 */
@Keep
class LightningObjectFactory {
    private var utils: Utils? = null

    /**
     * Reflection constructor. Call init after calling this
     */
    @Suppress("unused")
    constructor()

    constructor(utils: Utils) {
        this.utils = utils
    }

    fun init(eval: (String, Array<Any?>)->Unit, asFunction: (Any) -> Any) {
        this.utils = Utils(eval, asFunction)
    }

    operator fun get(className: String): JavaScript {
        try {
            if (MultiTool.DEBUG) Log.d(MultiTool.LOG_TAG, "ObjectFactory loading $className")
            @Suppress("UNCHECKED_CAST")
            val clazz = Class.forName(className) as Class<out JavaScript>
            return clazz.getConstructor(Utils::class.java).newInstance(utils)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    @FunctionalInterface
    interface FunctionFactory {
        fun asFunction(target: Any): Any
    }
}
