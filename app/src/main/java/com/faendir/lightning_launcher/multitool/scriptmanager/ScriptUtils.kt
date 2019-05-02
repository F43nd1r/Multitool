package com.faendir.lightning_launcher.multitool.scriptmanager

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import com.faendir.lightning_launcher.multitool.Loader
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.fastadapter.Model
import org.acra.util.StreamReader

import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.regex.Pattern

/**
 * Created by Lukas on 29.08.2015.
 * Backend for tasks
 */
internal object ScriptUtils {

    private const val FLAGS = "//Flags "
    private const val APP = "app "
    private const val ITEM = "item "
    private const val CUSTOM = "custom "
    private const val NAME = "Name:  "


    fun searchDialog(context: Context, listManager: ListManager) {
        val editText = EditText(context)
        AlertDialog.Builder(context).setTitle(R.string.title_search).setView(editText).setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok) { _, _ -> search(context, listManager.items, editText.text.toString()) }.show()
    }

    private fun search(context: Context, items: List<Model>, regex: String) {
        val builder = StringBuilder(context.getString(R.string.text_matches_lines))
        val pattern = Pattern.compile(regex)
        for (item in items) {
            if (item is Script) {
                var isFirst = true
                val lines = item.text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (i in lines.indices) {
                    if (pattern.matcher(lines[i]).find()) {
                        if (isFirst) {
                            builder.append(item.name).append(": ")
                            isFirst = false
                        } else {
                            builder.append(", ")
                        }
                        builder.append(i + 1)
                    }
                }
                if (!isFirst) {
                    builder.append("\n")
                }
            }
        }
        AlertDialog.Builder(context).setMessage(builder.toString().trim { it <= ' ' }).setTitle(R.string.title_matches).setNeutralButton(R.string.button_ok, null).show()
    }

    fun renameDialog(context: Context, listManager: ListManager, script: Script) {
        val text = EditText(context)
        text.setText(script.name)
        AlertDialog.Builder(context).setTitle(
                context.getString(R.string.title_rename, context.getString(R.string.text_script)))
                .setView(text).setPositiveButton(R.string.button_ok) { _, _ ->
                    script.name = text.text.toString()
                    MultiTool.get().doInLL { scriptService -> scriptService.updateScript(script.asLLScript()) }
                    listManager.deselectAll()
                }
                .setNegativeButton(R.string.button_cancel, null).show()
    }

    fun format(context: Context, listManager: ListManager, selectedItems: List<Model>) {
        FormatTask3(context).execute(*selectedItems.toTypedArray())
        listManager.deselectAll()
    }

    fun backup(context: Context, listManager: ListManager, script: Script, uri: Uri) {
        var prefix = FLAGS
        if (script.flags shr 1 and 1 == 1) {
            prefix += APP
        }
        if (script.flags shr 2 and 1 == 1) {
            prefix += ITEM
        }
        if (script.flags shr 3 and 1 == 1) {
            prefix += CUSTOM
        }
        prefix += " " + NAME + script.name + "\n"
        try {
            val outputStream = context.contentResolver.openOutputStream(uri) ?: throw FileNotFoundException()
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(prefix + script.text)
                writer.flush()
                Toast.makeText(context, R.string.text_backupSuccessful, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(context, R.string.text_backupFailed, Toast.LENGTH_SHORT).show()
        } finally {
            listManager.deselectAll()
        }
    }

    fun editScript(context: Context, listManager: ListManager, script: Script) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setClassName("net.pierrox.lightning_launcher_extreme", "net.pierrox.lightning_launcher.activities.ScriptEditor")
        intent.putExtra("i", script.id)
        context.startActivity(intent)
        listManager.deselectAll()
    }

    fun deleteScript(listManager: ListManager, delete: Script) {
        MultiTool.get().doInLL { scriptService ->
            scriptService.deleteScript(net.pierrox.lightning_launcher.api.Script(delete.id))
            listManager.update()
        }
    }

    fun restoreFromFile(context: Context, listManager: ListManager, uri: Uri) {
        try {
            val `in` = context.contentResolver.openInputStream(uri)
            if (`in` != null) {
                restoreDialog(context, listManager, StreamReader(`in`).read(), uri.lastPathSegment)
                return
            }
        } catch (ignored: IOException) {
        }

        Toast.makeText(context, context.getString(R.string.toast_failedLoad) + uri.path!!, Toast.LENGTH_SHORT).show()
    }

    private fun restoreDialog(context: Context, listManager: ListManager, s: String, filename: String?) {
        var content = s
        var endOfFirstLine = content.indexOf('\n')
        if (endOfFirstLine == -1) {
            endOfFirstLine = content.length
        }
        val l = content.substring(0, endOfFirstLine)
        var flags = 0
        if (l.contains(FLAGS)) { //check if file contains flag settings
            if (l.contains(APP)) {
                flags += Loader.FLAG_APP_MENU
            }
            if (l.contains(ITEM)) {
                flags += Loader.FLAG_ITEM_MENU
            }
            if (l.contains(CUSTOM)) {
                flags += Loader.FLAG_CUSTOM_MENU
            }
            content = content.substring(endOfFirstLine + 1)
        }
        var nameFromFile: String? = ""
        if (l.contains(NAME)) {
            nameFromFile = l.substring(l.indexOf(NAME) + NAME.length).trim { it <= ' ' }
        }
        if (nameFromFile == "") {
            //remove file extension
            nameFromFile = filename
            val index = nameFromFile!!.lastIndexOf('.')
            if (index != -1) {
                nameFromFile = nameFromFile.substring(0, index)
            }
        }
        //ask for imported scripts name
        val editText = EditText(context)
        editText.setText(nameFromFile)
        val finalS = content
        val finalFlags = flags
        AlertDialog.Builder(context).setView(editText).setTitle(R.string.title_chooseName).setPositiveButton(R.string.button_ok
        ) { dialog, ignore -> prepareRestore(context, listManager, finalS, editText.text.toString(), finalFlags) }
                .setNegativeButton(R.string.button_cancel, null).show()
    }

    private fun prepareRestore(context: Context, listManager: ListManager, code: String, name: String, flags: Int) {
        val script = Script(name, 0, code, flags, "/")
        if (listManager.exists(script)) {
            AlertDialog.Builder(context).setMessage(R.string.message_overwrite)
                    .setPositiveButton(R.string.button_ok) { dialog, ignore -> restore(listManager, script) }.setNegativeButton(R.string.button_cancel, null)
                    .show()
        } else {
            restore(listManager, script)
        }

    }

    private fun restore(listManager: ListManager, script: Script) {
        MultiTool.get().doInLL { scriptService ->
            scriptService.updateScript(net.pierrox.lightning_launcher.api.Script(script.text, script.name, script.path, script.flags))
            listManager.update()
        }
    }

    fun toggleDisable(listManager: ListManager, item: Script) {
        item.setFlag(net.pierrox.lightning_launcher.api.Script.FLAG_DISABLED, !item.hasFlag(net.pierrox.lightning_launcher.api.Script.FLAG_DISABLED))
        MultiTool.get().doInLL { scriptService ->
            scriptService.updateScript(item.asLLScript())
            listManager.deselectAll()
        }
    }
}
