package com.faendir.lightning_launcher.multitool.drawer

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import androidx.preference.MultiSelectListPreference

/**
 * @author F43nd1r
 * @since 13.11.2016
 */

class RestorePreference(context: Context, attrs: AttributeSet) : MultiSelectListPreference(context, attrs) {
    private val pm: PackageManager = getContext().packageManager

    init {
        isPersistent = true
        entries = arrayOfNulls(0)
        entryValues = arrayOfNulls(0)
    }

    private fun getLabelForComponent(flatComponent: String): String = pm.getActivityInfo(ComponentName.unflattenFromString(flatComponent), 0)?.loadLabel(pm).toString()

    override fun callChangeListener(newValue: Any): Boolean {
        val values = values
        values.removeAll(newValue as Collection<*>)
        super.callChangeListener(values)
        setValues(values)
        return false
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val values: Set<String>? = if (defaultValue is Collection<*>) getPersistedStringSet(defaultValue.map { it.toString() }.toMutableSet()) else emptySet()
        entryValues = values?.toTypedArray()
        entries = values?.map { this.getLabelForComponent(it) }?.toTypedArray()
        setValues(values)
    }
}
