package com.faendir.lightning_launcher.multitool.animation

/**
 * @author lukas
 * @since 10.07.18
 */
class PointB(val x: Boolean, val y: Boolean) {

    fun any(): Boolean = x || y

    fun both(): Boolean = x && y
}
