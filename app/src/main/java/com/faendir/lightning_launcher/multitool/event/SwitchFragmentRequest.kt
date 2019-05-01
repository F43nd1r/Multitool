package com.faendir.lightning_launcher.multitool.event

import androidx.annotation.StringRes
import com.faendir.lightning_launcher.multitool.util.Fragments

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
class SwitchFragmentRequest(val fragment: Fragments) {

    val id: Int
        @StringRes
        get() = fragment.res

    constructor(@StringRes id: Int) : this(Fragments.values().first { it.res == id })
}
