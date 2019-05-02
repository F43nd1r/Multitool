package com.faendir.lightning_launcher.multitool.proxy

import android.content.Context

/**
 * @author lukas
 * @since 04.07.18
 */
interface Screen : Proxy {

    val lastTouchX: Float

    val lastTouchY: Float

    val context: Context

    val currentDesktop: Desktop
    fun runAction(action: Int, data: String?)

    fun runScript(path: String, name: String, data: String)

    fun getContainerById(id: Int): Container
}
