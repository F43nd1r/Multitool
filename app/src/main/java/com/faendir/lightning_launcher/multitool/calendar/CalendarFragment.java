package com.faendir.lightning_launcher.multitool.calendar;

import android.Manifest;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.settings.PreferenceListener;
import com.faendir.lightning_launcher.scriptlib.PermissionActivity;
import com.google.common.util.concurrent.FutureCallback;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * @author lukas
 * @since 10.08.18
 */
public class CalendarFragment extends PreferenceFragmentCompat {
    private PreferenceListener listener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.calendar, rootKey);
        listener = new PreferenceListener(getPreferenceScreen());
        listener.addPreferenceForSummary(getString(R.string.pref_calendars));
        listener.addPreferenceForSummary(getString(R.string.pref_dateFormat));
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        PermissionActivity.checkForPermission(getActivity(), Manifest.permission.READ_CALENDAR).addCallback(new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@NullableDecl Boolean result) {
                if (result != null && result) {
                    ((CalendarPreference) getPreferenceScreen().findPreference(getString(R.string.pref_calendars))).refresh();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, Runnable::run);
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }
}
