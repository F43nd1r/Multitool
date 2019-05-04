package com.faendir.lightning_launcher.multitool.util.provider

import android.content.Context

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
interface DataSource {
    val path: String

    open fun init(context: Context) {}
}
