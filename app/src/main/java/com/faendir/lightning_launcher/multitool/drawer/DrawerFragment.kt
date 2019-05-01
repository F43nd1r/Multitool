package com.faendir.lightning_launcher.multitool.drawer

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.faendir.lightning_launcher.multitool.R

/**
 * @author F43nd1r
 * @since 01.11.2016
 */

class DrawerFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) = setPreferencesFromResource(R.xml.drawer, rootKey)
}
