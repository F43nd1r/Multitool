package com.faendir.lightning_launcher.multitool.util

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

/**
 * @author F43nd1r
 * @since 30.01.2018
 */

class ReflectionJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        try {
            val o = Class.forName(tag).newInstance()
            if (o is Job) {
                return o
            }
        } catch (ignored: Exception) {
        }

        return null
    }
}
