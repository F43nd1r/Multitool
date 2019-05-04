package com.faendir.lightning_launcher.multitool.util.provider

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.faendir.lightning_launcher.multitool.util.Utils
import com.faendir.lightning_launcher.multitool.util.provider.SharedPreferencesDataSource.Companion.COLUMN_KEY
import com.faendir.lightning_launcher.multitool.util.provider.SharedPreferencesDataSource.Companion.COLUMN_TYPE
import com.faendir.lightning_launcher.multitool.util.provider.SharedPreferencesDataSource.Companion.COLUMN_VALUE
import java.util.*

/**
 * @author lukas
 * @since 08.07.18
 */
class RemoteSharedPreferences(private val context: Context) : SharedPreferences {
    private val uri: Uri = DataProvider.getContentUri<SharedPreferencesDataSource>()

    override fun getAll(): Map<String, *> {
        val result = HashMap<String, Any>()
        context.contentResolver.query(uri, null, null, null, null)?.use {
            while (it.moveToNext()) {
                result[it.getString(it.getColumnIndex(COLUMN_KEY))] = Utils.GSON.fromJson(it.getString(it.getColumnIndex(COLUMN_VALUE)), Class.forName(it.getString(it.getColumnIndex(COLUMN_TYPE))))
            }
        }
        return result
    }

    override fun getString(key: String, defValue: String?): String? {
        return get(key, defValue)
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return get<Array<String>>(key, null)?.toMutableSet() ?: defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        return get(key, defValue)!!
    }

    override fun getLong(key: String, defValue: Long): Long {
        return get(key, defValue)!!
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return get(key, defValue)!!
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return get(key, defValue)!!
    }

    override fun contains(key: String): Boolean {
        return getString(key, null) != null
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor()
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        throw UnsupportedOperationException("Remote SharedPreferences do not support listeners")
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        throw UnsupportedOperationException("Remote SharedPreferences do not support listeners")
    }

    private inline fun <reified T> get(key: String, defValue: T?): T? {
        context.contentResolver.query(uri, null, null, arrayOf(key), null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return@get Utils.GSON.fromJson(cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)), T::class.java)
            }
        }
        return defValue
    }

    private inner class Editor internal constructor() : SharedPreferences.Editor {
        private val changes: MutableMap<String, Any?> = HashMap()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            changes[key] = value
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            changes[key] = values
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            changes[key] = value
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            changes[key] = value
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            changes[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            changes[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            changes[key] = null
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            for (key in all.keys) {
                remove(key)
            }
            return this
        }

        override fun commit(): Boolean {
            val contentValues = ContentValues()
            for ((key, value) in changes) {
                contentValues.put(key, Utils.GSON.toJson(value))
            }
            context.contentResolver.update(uri, contentValues, null, null)
            return true
        }

        override fun apply() {
            Thread { this.commit() }.start()
        }
    }
}
