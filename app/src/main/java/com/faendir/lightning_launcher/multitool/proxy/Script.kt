package com.faendir.lightning_launcher.multitool.proxy

/**
 * @author lukas
 * @since 05.07.18
 */
interface Script : Proxy {

    val id: Int

    val path: String

    var name: String

    var text: String

    fun run(screen: Screen, data: String)

    fun setFlag(flag: Int, on: Boolean)

    fun hasFlag(flag: Int): Boolean

    companion object {
        const val FLAG_DISABLED = 1
        const val FLAG_ALL = 0
        const val FLAG_APP_MENU = 2
        const val FLAG_ITEM_MENU = 4
        const val FLAG_CUSTOM_MENU = 8
    }
}
