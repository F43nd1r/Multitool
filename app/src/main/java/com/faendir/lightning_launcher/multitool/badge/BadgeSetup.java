package com.faendir.lightning_launcher.multitool.badge;

import android.app.Activity;
import android.content.Intent;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.ActivityScreen;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

/**
 * @author lukas
 * @since 08.07.18
 */
public class BadgeSetup {
    private final Utils utils;

    public BadgeSetup(Utils utils) {
        this.utils = utils;
    }

    public void show() {
        Script create = utils.installScript("badge", R.raw.badge_create, "create");
        ActivityScreen screen = ProxyFactory.cast(utils.getActiveScreen(), ActivityScreen.class);
        Container d = utils.getContainer();
        Intent intent = new Intent(utils.getMultitoolContext(), AppChooser.class);
        screen.startActivityForResult(intent, create, String.valueOf(d.getId()));
    }

    public void create(int resultCode, Intent data, String token) {
        if (resultCode == Activity.RESULT_OK) {
            Container d = utils.getActiveScreen().getContainerById(Integer.parseInt(token));
            Script resume = utils.installScript("badge", R.raw.badge_resume, "resume");
            Script pause = utils.installScript("badge", R.raw.badge_pause, "pause");
            Shortcut item = d.addShortcut("0", new Intent(), 0, 0);
            Intent intent = data.getParcelableExtra(Intent.EXTRA_INTENT);
            item.setTag("package", intent.getComponent().getPackageName());
            item.getProperties()
                    .edit()
                    .setBoolean("i.onGrid", false)
                    .setBoolean("s.iconVisibility", false)
                    .setBoolean("s.labelVisibility", true)
                    .setBoolean("i.enabled", false)
                    .setEventHandler("i.resumed", EventHandler.RUN_SCRIPT, String.valueOf(resume.getId()))
                    .setEventHandler("i.paused", EventHandler.RUN_SCRIPT, String.valueOf(pause.getId()))
                    .commit();
            utils.centerOnTouch(item);
        }
    }
}
