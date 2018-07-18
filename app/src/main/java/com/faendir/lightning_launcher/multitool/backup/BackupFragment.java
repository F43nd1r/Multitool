package com.faendir.lightning_launcher.multitool.backup;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.backup);
        listener = new PreferenceListener(getPreferenceScreen());
        Runnable backupChanged = () -> BackupUtils.scheduleNext(getActivity());
        listener.addPreference(getString(R.string.pref_backupTime), backupChanged);
        listener.addPreference(getString(R.string.pref_enableBackup), backupChanged);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                new AlertDialog.Builder(getActivity()).setTitle(R.string.title_help).setMessage(R.string.message_backupHelp).setPositiveButton(R.string.button_ok, null).show();
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }
}
