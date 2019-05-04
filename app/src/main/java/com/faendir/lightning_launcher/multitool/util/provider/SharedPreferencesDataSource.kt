package com.faendir.lightning_launcher.multitool.util.provider

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import androidx.preference.PreferenceManager
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.util.Utils
import java.util.*

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
open class SharedPreferencesDataSource : QueryDataSource, UpdateDataSource {

    override val path = "pref"

    override fun query(context: Context, uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val cursor = MatrixCursor(arrayOf(COLUMN_KEY, COLUMN_VALUE, COLUMN_VALUE))
        val values = sharedPref.all
        val args = selectionArgs ?: values.keys.toTypedArray()
        for (s in args) {
            values[s]?.let { cursor.addRow(arrayOf<Any>(s, Utils.GSON.toJson(it), s.javaClass.name)) }
        }
        return cursor
    }

    override fun update(context: Context, uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        if (values != null) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPref.edit()
            for (entry in values.valueSet()) {
                when(val value = entry.value) {
                    null -> editor.remove(entry.key)
                    is Boolean -> editor.putBoolean(entry.key, value)
                    is Float -> editor.putFloat(entry.key, value)
                    is Int -> editor.putInt(entry.key, value)
                    is Long -> editor.putLong(entry.key, value)
                    else -> {
                        try {
                            val strings = Utils.GSON.fromJson(value.toString(), Array<String>::class.java)
                            editor.putStringSet(entry.key, HashSet(Arrays.asList(*strings)))
                        } catch (e: Exception) {
                            editor.putString(entry.key, value.toString())
                        }
                    }
                }
            }
            editor.apply()
        }
        return values?.size() ?: 0
    }

    override fun init(context: Context) {
        PreferenceManager.setDefaultValues(context, R.xml.prefs, true)
        PreferenceManager.setDefaultValues(context, R.xml.drawer, true)
        PreferenceManager.setDefaultValues(context, R.xml.backup, true)
        PreferenceManager.setDefaultValues(context, R.xml.badge, true)
        PreferenceManager.setDefaultValues(context, R.xml.calendar, true)
        if (MultiTool.DEBUG) Log.d(MultiTool.LOG_TAG, "Loaded default pref values")
    }

    companion object {
        internal const val COLUMN_KEY = "key"
        internal const val COLUMN_VALUE = "value"
        internal const val COLUMN_TYPE = "type"
    }
}
