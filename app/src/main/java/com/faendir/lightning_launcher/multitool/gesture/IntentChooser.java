package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.BaseActivity;
import com.faendir.lightning_launcher.multitool.util.IntentChooserFragment;
import com.google.android.material.tabs.TabLayout;

public class IntentChooser extends BaseActivity {

    public IntentChooser() {
        super(R.layout.content_intent_chooser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Intent intent;
                boolean indirect;
                if (position == 0) {
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    indirect = false;
                } else {
                    intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
                    indirect = true;
                }
                return IntentChooserFragment.newInstance(intent, indirect);
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getString(position == 0 ? R.string.title_apps : R.string.title_shortcuts);
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
