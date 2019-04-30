package com.faendir.lightning_launcher.multitool.backup

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.JavaScript
import com.faendir.lightning_launcher.multitool.proxy.Utils
import org.acra.ACRAConstants
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author F43nd1r
 * @since 20.12.2017
 */
@Keep
class BackupCreator(private val utils: Utils) : JavaScript.Direct {

    @Throws(IOException::class)
    private fun copyDirectory(stream: ZipOutputStream, directory: File, prefix: String) {
        var prefix = prefix
        if (directory.exists()) {
            if (prefix.isNotEmpty()) prefix += "/"
            prefix += directory.name
            for (file in directory.listFiles()) {
                if (file.isDirectory) {
                    copyDirectory(stream, file, prefix)
                } else {
                    copyFile(stream, file, prefix)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFile(stream: ZipOutputStream, file: File, prefix: String) {
        if (file.exists()) {
            startEntry(stream, prefix + "/" + file.name)
            copy(FileInputStream(file), stream)
        }
    }

    @Throws(IOException::class)
    private fun addDirectoryEntry(stream: ZipOutputStream, name: String) {
        startEntry(stream, "$name/")
        stream.closeEntry()
    }

    @Throws(IOException::class)
    private fun startEntry(stream: ZipOutputStream, name: String) {
        stream.putNextEntry(ZipEntry(name))
    }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        BufferedInputStream(source).use {
            val buffer = ByteArray(ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES)
            var length = it.read(buffer, 0, buffer.size)
            while (length > 0) {
                target.write(buffer, 0, length)
                length = it.read(buffer, 0, buffer.size)
            }
        }
    }

    override fun execute(data: String): String {
        var result = false
        val root = Environment.getExternalStorageDirectory()
        if (root != null) {
            val directory = File(root, "LightningLauncher")

            directory.mkdirs()
            val file = File(directory, "Autobackup.lla")
            try {
                ZipOutputStream(BufferedOutputStream(FileOutputStream(file))).use { stream ->
                    stream.setMethod(ZipOutputStream.DEFLATED)
                    stream.setLevel(0)
                    startEntry(stream, "version")
                    copy(ByteArrayInputStream("1".toByteArray()), stream)
                    stream.closeEntry()
                    addDirectoryEntry(stream, CORE)
                    val lightning = utils.lightningContext.filesDir
                    copyDirectory(stream, File(lightning, "pages"), CORE)
                    copyDirectory(stream, File(lightning, "scripts"), CORE)
                    copyDirectory(stream, File(lightning, "themes"), CORE)
                    copyFile(stream, File(lightning, "config"), CORE)
                    copyFile(stream, File(lightning, "state"), CORE)
                    copyFile(stream, File(lightning, "statistics"), CORE)
                    copyFile(stream, File(lightning, "system"), CORE)
                    copyFile(stream, File(lightning, "variables"), CORE)
                    addDirectoryEntry(stream, WALLPAPER)
                    val wallpaper = drawableToBitmap(WallpaperManager.getInstance(utils.lightningContext).drawable)
                    startEntry(stream, WALLPAPER + File.separator + "bitmap.png")
                    wallpaper.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.closeEntry()
                    addDirectoryEntry(stream, FONTS)
                    copyDirectory(stream, File(lightning, FONTS), "")
                    addDirectoryEntry(stream, "widgets_data")
                    result = true
                }
            } catch (e: IOException) {
                Log.e(MultiTool.LOG_TAG, "Skipped backup because of exception during write", e)
            }

        } else {
            Log.i(MultiTool.LOG_TAG, "Skipped backup because directory is not available")
        }
        Toast.makeText(utils.multitoolContext, if (result) utils.getString(R.string.toast_backupSuccess) else utils.getString(R.string.toast_backupFail), Toast.LENGTH_LONG).show()
        return ""
    }

    companion object {
        private const val CORE = "core"
        private const val FONTS = "fonts"
        private const val WALLPAPER = "wallpaper"

        private fun drawableToBitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) {
                if (drawable.bitmap != null) {
                    return drawable.bitmap
                }
            }
            val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}
