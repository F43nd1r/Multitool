package com.faendir.lightning_launcher.multitool.util.provider

import android.content.Context

import java.io.File

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

interface FileDataSource : DataSource {
    fun getFile(context: Context): File
}
