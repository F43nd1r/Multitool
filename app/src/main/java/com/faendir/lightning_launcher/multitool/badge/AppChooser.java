package com.faendir.lightning_launcher.multitool.badge;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.BaseActivity;
import com.faendir.lightning_launcher.multitool.util.IntentChooserFragment;
import com.faendir.lightning_launcher.multitool.util.notification.NotificationDistributorService;

import java.util.Collections;
import java.util.Set;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * @author F43nd1r
 * @since 07.11.2017
 */

public class AppChooser extends BaseActivity {
    private boolean byRelevance;

    public AppChooser() {
        super(R.layout.content_app_chooser);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_appChooser);
        byRelevance = true;
        sort();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && !NotificationDistributorService.isEnabled(this)) {
            NotificationDistributorService.askForEnable(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_badge_app_chooser, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void sort() {
        IntentChooserFragment fragment = (IntentChooserFragment) getSupportFragmentManager().findFragmentById(R.id.chooserFragment);
        if (byRelevance) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Set<String> highPriority = sharedPref.getStringSet(getString(R.string.key_badgeIntentPackages), Collections.emptySet());
            String prefix = getString(R.string.unread_prefix);
            Set<String> midPriority = StreamSupport.stream(sharedPref.getAll().keySet()).filter(key -> key.startsWith(prefix)).map(key -> key.substring(prefix.length())).collect(Collectors.toSet());
            fragment.setComparator((o1, o2) -> {
                String pn1 = o1.getIntent().getComponent().getPackageName();
                String pn2 = o2.getIntent().getComponent().getPackageName();
                int p1 = highPriority.contains(pn1) ? 2 : midPriority.contains(pn1) ? 1 : 0;
                int p2 = highPriority.contains(pn2) ? 2 : midPriority.contains(pn2) ? 1 : 0;
                int compare = Integer.compare(p2, p1);
                return compare != 0 ? compare : o1.compareTo(o2);
            });
        } else {
            fragment.setComparator(((o1, o2) -> o1.getName().compareTo(o2.getName())));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_change_sorting) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.action_change_sorting)
                    .setSingleChoiceItems(R.array.sortings, byRelevance ? 0 : 1, (dialog, which) -> {
                        byRelevance = which == 0;
                        sort();
                        dialog.dismiss();
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }
}