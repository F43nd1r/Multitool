package com.faendir.lightning_launcher.multitool.backup;

import org.apache.commons.collections4.list.UnmodifiableList;

import java.util.List;

/**
 * Created on 23.07.2016.
 *
 * @author F43nd1r
 */
public class BackupTime {
    private final int hour;
    private final int minute;
    private final List<Integer> days;

    public BackupTime(int hour, int minute, List<Integer> days) {
        this.hour = hour;
        this.minute = minute;
        this.days = new UnmodifiableList<>(days);
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public List<Integer> getDays() {
        return days;
    }
}
