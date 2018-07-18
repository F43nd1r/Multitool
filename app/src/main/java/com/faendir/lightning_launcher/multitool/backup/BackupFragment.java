package com.faendir.lightning_launcher.multitool.backup;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.settings.PreferenceListener;

/**
 * @author lukas
 * @since 18.07.18
 */
public class BackupFragment extends PreferenceFragment {
    private PreferenceListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.backup);
        listener = new PreferenceListener(getPreferenceScreen());
        Runnable backupChanged = () -> BackupUtils.scheduleNext(getActivity());
        listener.addPreference(getString(R.string.pref_backupTime), backupChanged);
        listener.addPreference(getString(R.string.pref_enableBackup), backupChanged);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }
}
