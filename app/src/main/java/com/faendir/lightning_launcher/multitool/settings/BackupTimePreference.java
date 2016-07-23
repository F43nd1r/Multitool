package com.faendir.lightning_launcher.multitool.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.backup.BackupTime;
import com.faendir.lightning_launcher.multitool.backup.BackupUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackupTimePreference extends DialogPreference implements View.OnClickListener {
    private BackupTime backupTime = BackupUtils.getBackupTime(null);
    private Map<Button, Integer> map;
    private TimePicker picker;

    public BackupTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.preference_backup_time, null);
        map = new HashMap<>();
        map.put((Button) root.findViewById(R.id.buttonMonday), Calendar.MONDAY);
        map.put((Button) root.findViewById(R.id.buttonTuesday), Calendar.TUESDAY);
        map.put((Button) root.findViewById(R.id.buttonWednesday), Calendar.WEDNESDAY);
        map.put((Button) root.findViewById(R.id.buttonThursday), Calendar.THURSDAY);
        map.put((Button) root.findViewById(R.id.buttonFriday), Calendar.FRIDAY);
        map.put((Button) root.findViewById(R.id.buttonSaturday), Calendar.SATURDAY);
        map.put((Button) root.findViewById(R.id.buttonSunday), Calendar.SUNDAY);
        for (Button b : map.keySet()) {
            b.setOnClickListener(this);
        }
        picker = (TimePicker) root.findViewById(R.id.timePicker);
        picker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        return root;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        picker.setCurrentHour(backupTime.getHour());
        picker.setCurrentMinute(backupTime.getMinute());
        for (Map.Entry<Button, Integer> entry : map.entrySet()) {
            entry.getKey().setSelected(backupTime.getDays().contains(entry.getValue()));
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            List<Integer> days = new ArrayList<>();
            for (Map.Entry<Button, Integer> entry : map.entrySet()) {
                if (entry.getKey().isSelected()) days.add(entry.getValue());
            }
            backupTime = new BackupTime(picker.getCurrentHour(), picker.getCurrentMinute(), days);
            String time = BackupUtils.toString(backupTime);

            if (callChangeListener(time)) {
                persistString(time);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;

        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString(null);
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        }
        backupTime = BackupUtils.getBackupTime(time);
    }

    @Override
    public void onClick(View view) {
        view.setSelected(!view.isSelected());
    }

}