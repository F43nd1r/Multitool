package com.faendir.lightning_launcher.multitool.proxy

/**
 * @author lukas
 * @since 04.07.18
 */
interface Event : Proxy {
    val item: Item?

    val source: String

    val date: Long

    val container: Container

    val screen: Screen

    val data: String

    val touchX: Float

    val touchY: Float

    val touchScreenX: Float

    val touchScreenY: Float
}
