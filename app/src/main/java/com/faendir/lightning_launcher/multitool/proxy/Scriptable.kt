package com.faendir.lightning_launcher.multitool.proxy

/**
 * @author lukas
 * @since 17.07.18
 */
interface Scriptable : Proxy {
    operator fun get(key: String, start: Scriptable): Any

    fun put(key: String, start: Scriptable, value: Any)
}
