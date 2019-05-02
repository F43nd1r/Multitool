package com.faendir.lightning_launcher.multitool.gesture

import android.content.Context

import com.faendir.lightning_launcher.multitool.util.provider.FileDataSource

import java.io.File

/**
 * @author F43nd1r
 * @since 06.11.2017
 */

class GestureLibraryDataSource : FileDataSource {

    override fun getPath(): String = "lib"

    override fun getFile(context: Context): File = File(context.filesDir, "gestureLibrary")
}
