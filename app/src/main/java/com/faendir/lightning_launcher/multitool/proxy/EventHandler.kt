package com.faendir.lightning_launcher.multitool.proxy

import android.content.Context

/**
 * @author lukas
 * @since 05.07.18
 */
interface EventHandler : Proxy {

    val action: Int

    val data: String?

    fun setNext(next: EventHandler?)

    companion object {
        const val RUN_SCRIPT = 35
        const val RESTART = 28
        const val UNSET = 0

        fun newInstance(context: Context, action: Int, data: String): EventHandler {
            try {
                return ProxyFactory.lightningProxy(context.classLoader
                        .loadClass("net.pierrox.lightning_launcher.script.api.EventHandler")
                        .getConstructor(Int::class.javaPrimitiveType, String::class.java)
                        .newInstance(action, data), EventHandler::class.java)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
    }
}
