package com.faendir.lightning_launcher.multitool;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.faendir.lightning_launcher.multitool.gesture.GestureFragment;
import com.faendir.lightning_launcher.multitool.launcherscript.LauncherScriptFragment;
import com.faendir.lightning_launcher.multitool.scriptmanager.ScriptManagerFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Fragment currentFragment;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        int load = R.id.nav_launcher_script;
        if (sharedPref.contains(getString(R.string.pref_lastFragment))) {
            load = getResources().getIdentifier(sharedPref.getString(getString(R.string.pref_lastFragment), null), "id", getPackageName());
        }
        switchTo(load);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_launcher_script:
            case R.id.nav_script_manager:
            case R.id.nav_gesture:
            //case R.id.nav_view_creator:
                switchTo(item.getItemId());
                break;
            case R.id.nav_email: {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        getString(R.string.link_email_scheme), getString(R.string.link_email_adress), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.link_email_subject));
                Intent intent = Intent.createChooser(emailIntent, getString(R.string.link_email_chooser));
                startActivity(intent);
                finish();
                break;
            }
            case R.id.nav_play_store: {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                finish();
                break;
            }
            case R.id.nav_community: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_googlePlus)));
                startActivity(intent);
                finish();
                break;
            }
            case R.id.nav_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchTo(int id) {
        if (currentFragment != null && sharedPref.getString(getString(R.string.pref_lastFragment), "").equals(getResources().getResourceName(id))) {
            return;
        }
        switch (id) {
            case R.id.nav_script_manager:
                currentFragment = new ScriptManagerFragment();
                break;
            case R.id.nav_launcher_script:
                currentFragment = new LauncherScriptFragment();
                break;
            case R.id.nav_gesture:
                currentFragment = new GestureFragment();
                break;
            /*case R.id.nav_view_creator:
                currentFragment = new ViewCreatorFragment();
                break;*/
            default:
                throw new IllegalArgumentException("Illegal id " + id);
        }
        getFragmentManager().beginTransaction().replace(R.id.content_frame, currentFragment).commit();
        sharedPref.edit().putString(getString(R.string.pref_lastFragment), getResources().getResourceName(id)).apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (currentFragment instanceof ScriptManagerFragment) {
            ((ScriptManagerFragment) currentFragment).onNewIntent(intent);
        }
    }

    public void onButtonClick(View v) {
        if (currentFragment instanceof ScriptManagerFragment) {
            ((ScriptManagerFragment) currentFragment).onReloadButton();
        } else if (currentFragment instanceof LauncherScriptFragment) {
            ((LauncherScriptFragment) currentFragment).onButtonClick(v);
        }
    }
}
