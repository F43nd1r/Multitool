package com.faendir.lightning_launcher.multitool.proxy

import android.view.View

/**
 * @author lukas
 * @since 08.07.18
 */
interface Menu : Proxy {

    val mode: Int

    fun close()

    fun addMainItem(text: String, action: Function): View

    companion object {
        const val MODE_ITEM_NO_EM = 12
        const val MODE_ITEM_EM = 2
    }
}
