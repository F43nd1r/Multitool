package com.faendir.lightning_launcher.multitool.backup;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import com.faendir.lightning_launcher.multitool.R;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lukas
 * @since 18.07.18
 */
public class BackupTimePreference extends Preference implements View.OnClickListener {
    private BackupTime backupTime;
    private Map<Button, Integer> map;
    private TimePicker picker;
    private boolean userOriginated = true;

    public BackupTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_backup_time);
        backupTime = BackupUtils.getBackupTime(null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        map = new HashMap<>();
        map.put(view.findViewById(R.id.buttonMonday), Calendar.MONDAY);
        map.put(view.findViewById(R.id.buttonTuesday), Calendar.TUESDAY);
        map.put(view.findViewById(R.id.buttonWednesday), Calendar.WEDNESDAY);
        map.put(view.findViewById(R.id.buttonThursday), Calendar.THURSDAY);
        map.put(view.findViewById(R.id.buttonFriday), Calendar.FRIDAY);
        map.put(view.findViewById(R.id.buttonSaturday), Calendar.SATURDAY);
        map.put(view.findViewById(R.id.buttonSunday), Calendar.SUNDAY);
        StreamSupport.stream(map.keySet()).forEach(button -> button.setOnClickListener(this));
        picker = view.findViewById(R.id.timePicker);
        picker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        picker.setOnTimeChangedListener((v, h, m) -> persist());
        showValue();
    }

    private void showValue() {
        userOriginated = false;
        for (Map.Entry<Button, Integer> entry : map.entrySet()) {
            entry.getKey().setSelected(backupTime.getDays().contains(entry.getValue()));
        }
        picker.setCurrentHour(backupTime.getHour());
        picker.setCurrentMinute(backupTime.getMinute());
        userOriginated = true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        showValue();
    }

    private void persist() {
        if(userOriginated) {
            List<Integer> days = StreamSupport.stream(map.entrySet()).filter(entry -> entry.getKey().isSelected()).map(Map.Entry::getValue).collect(Collectors.toList());
            backupTime = new BackupTime(picker.getCurrentHour(), picker.getCurrentMinute(), days);
            String time = BackupUtils.toString(backupTime);
            if (callChangeListener(time)) {
                persistString(time);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;

        if (restoreValue) {
            time = getPersistedString(null);
        }
        backupTime = BackupUtils.getBackupTime(time);
    }

    @Override
    public void onClick(View view) {
        view.setSelected(!view.isSelected());
        persist();
    }
}
