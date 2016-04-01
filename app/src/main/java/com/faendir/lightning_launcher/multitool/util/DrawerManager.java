package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.SettingsActivity;
import com.faendir.lightning_launcher.multitool.event.LeaveApplicationRequest;
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Lukas on 01.04.2016.
 */
public class DrawerManager implements NavigationView.OnNavigationItemSelectedListener {
    private final Context context;
    private final DrawerLayout drawerLayout;

    public DrawerManager(@NonNull Context context, @NonNull DrawerLayout drawerLayout) {
        this.context = context;
        this.drawerLayout = drawerLayout;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_launcher_script:
            case R.id.nav_script_manager:
            case R.id.nav_gesture:
            case R.id.nav_view_creator:
                EventBus.getDefault().post(new SwitchFragmentRequest(item.getItemId(), item.getTitle().toString()));
                break;
            case R.id.nav_email: {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        context.getString(R.string.link_email_scheme), context.getString(R.string.link_email_adress), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.link_email_subject));
                Intent intent = Intent.createChooser(emailIntent, context.getString(R.string.link_email_chooser));
                context.startActivity(intent);
                EventBus.getDefault().post(new LeaveApplicationRequest());
                break;
            }
            case R.id.nav_play_store: {
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
                } catch (android.content.ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
                }
                EventBus.getDefault().post(new LeaveApplicationRequest());
                break;
            }
            case R.id.nav_community: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.link_googlePlus)));
                context.startActivity(intent);
                EventBus.getDefault().post(new LeaveApplicationRequest());
                break;
            }
            case R.id.nav_settings: {
                context.startActivity(new Intent(context, SettingsActivity.class));
                break;
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else {
            return false;
        }
    }
}
