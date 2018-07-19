package com.faendir.lightning_launcher.multitool.badge;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.faendir.lightning_launcher.multitool.R;

/**
 * @author lukas
 * @since 19.07.18
 */
public class BadgeFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.badge);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.badge, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                new AlertDialog.Builder(getActivity()).setTitle(R.string.title_help).setMessage(R.string.message_helpBadge).setPositiveButton(R.string.button_ok, null).show();
                break;
        }
        return true;
    }
}
