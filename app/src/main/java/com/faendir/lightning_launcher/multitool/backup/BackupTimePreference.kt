package com.faendir.lightning_launcher.multitool.backup

import android.content.Context
import android.content.res.TypedArray
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TimePicker
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.faendir.lightning_launcher.multitool.R
import java.util.*

/**
 * @author lukas
 * @since 18.07.18
 */
class BackupTimePreference(context: Context, attrs: AttributeSet) : Preference(context, attrs), View.OnClickListener {
    private var backupTime: BackupTime
    private lateinit var buttons: MutableMap<Button, Int>
    private lateinit var picker: TimePicker
    private var userOriginated = true

    init {
        layoutResource = R.layout.preference_backup_time
        backupTime = BackupUtils.getBackupTime(null)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        buttons = HashMap()
        buttons[holder.findViewById(R.id.buttonMonday) as Button] = Calendar.MONDAY
        buttons[holder.findViewById(R.id.buttonTuesday) as Button] = Calendar.TUESDAY
        buttons[holder.findViewById(R.id.buttonWednesday) as Button] = Calendar.WEDNESDAY
        buttons[holder.findViewById(R.id.buttonThursday) as Button] = Calendar.THURSDAY
        buttons[holder.findViewById(R.id.buttonFriday) as Button] = Calendar.FRIDAY
        buttons[holder.findViewById(R.id.buttonSaturday) as Button] = Calendar.SATURDAY
        buttons[holder.findViewById(R.id.buttonSunday) as Button] = Calendar.SUNDAY
        buttons.keys.forEach { it.setOnClickListener(this) }
        picker = holder.findViewById(R.id.timePicker) as TimePicker
        picker.setIs24HourView(DateFormat.is24HourFormat(context))
        picker.setOnTimeChangedListener { v, h, m -> persist() }
        showValue()
    }

    private fun showValue() {
        userOriginated = false
        for ((key, value) in buttons) {
            key.isSelected = backupTime.days.contains(value)
        }
        picker.currentHour = backupTime.hour
        picker.currentMinute = backupTime.minute
        userOriginated = true
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        showValue()
    }

    private fun persist() {
        if (userOriginated) {
            val days = buttons.entries.filter { it.key.isSelected }.map { it.value }.toList()
            backupTime = BackupTime(picker.currentHour, picker.currentMinute, days)
            val time = BackupUtils.toString(backupTime)
            if (callChangeListener(time)) {
                persistString(time)
            }
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? = a.getString(index)

    override fun onSetInitialValue(defaultValue: Any?) {
        backupTime = BackupUtils.getBackupTime(getPersistedString(null))
    }

    override fun onClick(view: View) {
        view.isSelected = !view.isSelected
        persist()
    }
}
