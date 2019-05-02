package com.faendir.lightning_launcher.multitool.proxy

/**
 * @author lukas
 * @since 09.07.18
 */
interface CustomView : Item {
    fun setHorizontalGrab(grab: Boolean)

    fun setVerticalGrab(grab: Boolean)
}
