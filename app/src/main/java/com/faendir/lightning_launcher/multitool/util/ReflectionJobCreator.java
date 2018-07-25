package com.faendir.lightning_launcher.multitool.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * @author F43nd1r
 * @since 30.01.2018
 */

public class ReflectionJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        try {
            Object o = Class.forName(tag).newInstance();
            if(o instanceof Job){
                return (Job) o;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
