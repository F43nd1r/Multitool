package com.faendir.lightning_launcher.multitool.proxy

import android.content.Intent

/**
 * @author lukas
 * @since 04.07.18
 */
interface Shortcut : Item {
    var label: String

    val intent: Intent

    var defaultIcon: Image

    val customIcon: Image
}
