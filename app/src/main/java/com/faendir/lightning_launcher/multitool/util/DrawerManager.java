package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.greenrobot.eventbus.EventBus;

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
public class DrawerManager implements Drawer.OnDrawerItemClickListener {
    private final Context context;
    private final Drawer drawer;

    public DrawerManager(@NonNull Context context, Drawer drawer) {
        this.context = context;
        this.drawer = drawer;
        drawer.setOnDrawerItemClickListener(this);
    }

    public boolean closeDrawer() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return true;
        } else {
            return false;
        }
    }

    public void openDrawer() {
        if (!drawer.isDrawerOpen()) {
            drawer.openDrawer();
        }
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        switch ((int) drawerItem.getIdentifier()) {
            case R.string.title_launcherScript:
            case R.string.title_scriptManager:
            case R.string.title_gestureLauncher:
            case R.string.title_musicWidget:
            case R.string.title_settings:
            case R.string.title_drawer:
            case R.string.title_backup:
                EventBus.getDefault().post(new SwitchFragmentRequest((int) drawerItem.getIdentifier()));
                return false;
            case R.string.email: {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        context.getString(R.string.link_email_scheme), context.getString(R.string.link_email_adress), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.link_email_subject));
                Intent intent = Intent.createChooser(emailIntent, context.getString(R.string.link_email_chooser));
                context.startActivity(intent);
                break;
            }
            case R.string.play_store: {
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
                } catch (android.content.ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
                }
                break;
            }
            case R.string.google_community: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.link_googlePlus)));
                context.startActivity(intent);
                break;
            }
        }
        return true;
    }
}
