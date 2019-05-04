package com.faendir.lightning_launcher.multitool.util.view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import com.faendir.lightning_launcher.multitool.R
import java.lang.reflect.InvocationTargetException

/**
 * @author lukas
 * @since 18.07.18
 */
class CanDisableTimePicker : TimePicker {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context) : super(context)

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        val radial = findViewById<View>(getSystemId("radial_picker"))
        if (radial != null) {
            try {
                radial.javaClass.getMethod("setInputEnabled", Boolean::class.javaPrimitiveType!!).invoke(radial, enabled)
            } catch (ignored: NoSuchMethodException) {
            } catch (ignored: IllegalAccessException) {
            } catch (ignored: InvocationTargetException) {
            }

        }
        val textViews = intArrayOf(getSystemId("hours"), getSystemId("minutes"), getSystemId("separator"), getSystemId("am_label"), getSystemId("pm_label"))
        val colors = ContextCompat.getColorStateList(context, R.color.timepicker_text_color)
        for (id in textViews) {
            val view = findViewById<TextView>(id)
            view?.setTextColor(colors)
        }
    }

    private fun getSystemId(name: String): Int = Resources.getSystem().getIdentifier(name, "id", "android")
}
