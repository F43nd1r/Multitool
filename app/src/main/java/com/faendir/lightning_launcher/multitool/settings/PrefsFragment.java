package com.faendir.lightning_launcher.multitool.settings;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.faendir.lightning_launcher.multitool.BuildConfig;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.backup.BackupUtils;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Lukas on 29.08.2015.
 * preference fragment
 */
public class PrefsFragment extends PreferenceFragment {
    public static final String DEFAULT_BACKUP_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "LightningLauncher" + File.separator + "Scripts";

    private SharedPreferences sharedPref;
    private PreferenceListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        listener = new PreferenceListener(getPreferenceScreen());
        String path = sharedPref.getString(getString(R.string.pref_directory), PrefsFragment.DEFAULT_BACKUP_PATH);
        Preference dir = findPreference(getString(R.string.pref_directory));
        dir.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                intent.putExtra(FilePickerActivity.EXTRA_START_PATH, sharedPref.getString(getString(R.string.pref_directory), PrefsFragment.DEFAULT_BACKUP_PATH));
                startActivityForResult(intent, 0);
                return true;
            }
        });
        listener.addPreferenceForSummary(dir);
        dir.setSummary(path);
        Runnable backupChanged = new Runnable() {
            @Override
            public void run() {
                BackupUtils.scheduleNext(getActivity());
            }
        };
        listener.addPreference(getString(R.string.key_backupTime), backupChanged, false);
        listener.addPreference(getString(R.string.key_enableBackup), backupChanged, false);
        if (!BuildConfig.DEBUG) {
            removeDebugOptions();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            setBackupPath(uri);
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            setBackupPath(uri);
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                setBackupPath(uri);
            }
        }
    }

    private void setBackupPath(Uri path) {
        sharedPref.edit().putString(getString(R.string.pref_directory), path.getEncodedPath()).apply();
    }

    private void removeDebugOptions() {
        //remove enable acra preference
        CheckBoxPreference acraPref = (CheckBoxPreference) findPreference(getString(R.string.pref_enableAcra));
        acraPref.setChecked(true);
        getPreferenceScreen().removePreference(acraPref);
    }
}
