package com.faendir.lightning_launcher.multitool.util

import android.content.Context
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.JavaScript

/**
 * @author lukas
 * @since 09.02.19
 */
class ScriptBuilder {
    private val builder = StringBuilder()

    fun addVariable(name: String, value: String): ScriptBuilder {
        builder.append("var ").append(name).append(" = ").append(value).append(";\n")
        return this
    }

    fun loadLibrary(context: Context): ScriptBuilder {
        builder.append(Utils.readRawResource(context, R.raw.library)).append("\n")
        return this
    }

    fun addStatement(statement: String): ScriptBuilder {
        builder.append(statement).append("\n")
        return this
    }

    override fun toString(): String {
        return builder.toString()
    }

    companion object {
        fun scriptForClass(context: Context, clazz: Class<out JavaScript.Direct>): String {
            return ScriptBuilder()
                    .loadLibrary(context)
                    .addStatement("return getObjectFactory().get('" + clazz.canonicalName + "').execute(null);")
                    .toString()
        }
    }
}
