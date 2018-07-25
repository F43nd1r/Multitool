package com.faendir.lightning_launcher.multitool.drawer;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.faendir.lightning_launcher.multitool.R;

/**
 * @author F43nd1r
 * @since 01.11.2016
 */

public class DrawerFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.drawer, rootKey);
    }
}
