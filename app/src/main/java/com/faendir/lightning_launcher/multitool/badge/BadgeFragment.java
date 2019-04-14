package com.faendir.lightning_launcher.multitool.badge;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.preference.PreferenceFragmentCompat;
import com.faendir.lightning_launcher.multitool.R;

/**
 * @author lukas
 * @since 19.07.18
 */
public class BadgeFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setHasOptionsMenu(true);
        setPreferencesFromResource(R.xml.badge, rootKey);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.badge, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_help) {
            new AlertDialog.Builder(getActivity()).setTitle(R.string.title_help).setMessage(R.string.message_helpBadge).setPositiveButton(R.string.button_ok, null).show();
        }
        return true;
    }
}
