package com.faendir.lightning_launcher.multitool.gesture

import android.content.Context
import android.gesture.GestureStore
import android.net.Uri
import android.widget.Toast
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.util.Utils
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Created on 28.01.2016.
 *
 * @author F43nd1r
 */
internal object GestureUtils {

    private const val METADATA = "metadata"
    private const val GESTURES = "gestures"

    fun delete(context: Context, selected: GestureInfo, list: MutableList<GestureInfo>) {
        selected.removeGesture(context)
        list.remove(selected)
        writeToFile(context, list)
    }

    fun exportGestures(context: Context, path: Uri) {
        try {
            val metadata = DataProvider.openFileForRead(context, GestureMetaDataSource::class.java)
            val gestures = DataProvider.openFileForRead(context, GestureLibraryDataSource::class.java)
            val outputStream = context.contentResolver.openOutputStream(path)
            if (outputStream != null) {
                try {
                    ZipOutputStream(BufferedOutputStream(outputStream)).use { out ->
                        out.putNextEntry(ZipEntry(METADATA))
                        writeAndClose(metadata, out)
                        out.putNextEntry(ZipEntry(GESTURES))
                        writeAndClose(gestures, out)
                        Toast.makeText(context, context.getString(R.string.toast_exportedTo, path.toString()), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    Toast.makeText(context, context.getString(R.string.toast_failedExportTo, path.toString()), Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(context, R.string.toast_fileNotWritable, Toast.LENGTH_SHORT).show()
            }
        } catch (e: FileNotFoundException) {
            Toast.makeText(context, R.string.toast_noGestures, Toast.LENGTH_SHORT).show()
        }

    }

    @Throws(IOException::class)
    private fun writeAndClose(input: InputStream, out: OutputStream) {
        BufferedInputStream(input).use { it.copyTo(out) }
    }

    fun importGestures(context: Context, path: Uri, list: MutableList<GestureInfo>) {
        try {
            val inputStream = context.contentResolver.openInputStream(path) ?: throw FileNotFoundException()
            val inStream = ZipInputStream(inputStream)
            var library: GestureStore? = null
            var gestureInfos: List<GestureInfo>? = null
            var entry = inStream.nextEntry
            while (entry  != null) {
                when (entry.name) {
                    METADATA -> gestureInfos = Arrays.asList(*Utils.GSON.fromJson(InputStreamReader(inStream), Array<GestureInfo>::class.java))
                    GESTURES -> {
                        library = GestureStore()
                        library.load(inStream, false)
                    }
                    else -> throw IOException()
                }
                inStream.closeEntry()
                entry = inStream.nextEntry
            }
            inStream.close()
            if (library == null || gestureInfos == null) {
                throw IOException()
            }
            if (gestureInfos.isNotEmpty()) {
                for (info in gestureInfos) {
                    info.setGesture(context, library.getGestures(info.uuid.toString())[0])
                    list.add(info)
                }
                writeToFile(context, list)
                Toast.makeText(context, R.string.toast_imported, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(context, R.string.toast_importFailed, Toast.LENGTH_SHORT).show()
        }
    }

    fun readFromFile(context: Context): List<GestureInfo> {
        try {
            InputStreamReader(DataProvider.openFileForRead(context, GestureMetaDataSource::class.java)).use { reader ->
                val array = Utils.GSON.fromJson(reader, Array<GestureInfo>::class.java)
                if (array != null) {
                    return array.toList()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return emptyList()
    }

    fun writeToFile(context: Context, infos: List<GestureInfo>) {
        try {
            BufferedWriter(OutputStreamWriter(DataProvider.openFileForWrite(context, GestureMetaDataSource::class.java))).use { writer ->
                Utils.GSON.toJson(infos.toTypedArray(), Array<GestureInfo>::class.java, writer)
                writer.flush()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }
}
