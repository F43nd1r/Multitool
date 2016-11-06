package com.faendir.lightning_launcher.multitool.drawer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.faendir.lightning_launcher.multitool.R;

/**
 * @author F43nd1r
 * @since 01.11.2016
 */

public class DrawerFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.drawer);
    }
}
