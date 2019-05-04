package com.faendir.lightning_launcher.multitool.scriptmanager

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.faendir.lightning_launcher.multitool.Loader
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.fastadapter.DeletableModel


/**
 * Created by Lukas on 22.08.2015.
 * Represents a script
 */
@Keep
class Script(name: String, id: Int, code: String, flags: Int, path: String) : Comparable<Script>, DeletableModel {
    private val delegate = net.pierrox.lightning_launcher.api.Script(id, code, name, path, flags)

    override var name: String
        get() = delegate.name
        set(value) {
            delegate.name = value
        }

    val id: Int
        get() = delegate.id

    val flags: Int
        get() = delegate.flags

    var text: String
        get() = delegate.text
        set(value) {
            delegate.text = value
        }

    val path: String
        get() = delegate.path

    override val tintColor: Int
        get() = if (isDisabled) Color.RED else Color.WHITE

    val isDisabled: Boolean
        get() = flags and Loader.FLAG_DISABLED != 0

    constructor(script: net.pierrox.lightning_launcher.api.Script) : this(script.name, script.id, script.text, script.flags, script.path)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val script = other as Script
        return id == script.id
    }

    override fun hashCode(): Int = id

    override fun compareTo(other: Script): Int = name.toLowerCase().compareTo(other.name.toLowerCase())

    override fun getUndoText(context: Context): String = context.getString(R.string.text_scriptDeleted)

    override fun getIcon(context: Context): Drawable = ContextCompat.getDrawable(context, R.drawable.ic_file_white)!!

    fun setFlag(flag: Int, on: Boolean) = delegate.setFlag(flag, on)

    fun hasFlag(flag: Int) = delegate.hasFlag(flag)

    fun asLLScript() = delegate
}
