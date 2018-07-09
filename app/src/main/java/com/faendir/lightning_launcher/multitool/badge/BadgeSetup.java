package com.faendir.lightning_launcher.multitool.badge;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Keep;
import com.faendir.lightning_launcher.multitool.proxy.ActivityScreen;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

/**
 * @author lukas
 * @since 08.07.18
 */
@Keep
public class BadgeSetup implements JavaScript.Setup, JavaScript.ActivityResult {
    static final String TAG_PACKAGE = "package";
    private final Utils utils;

    public BadgeSetup(Utils utils) {
        this.utils = utils;
    }

    @Override
    public void setup() {
        Script create = utils.installActivityResultScript();
        ActivityScreen screen = ProxyFactory.cast(utils.getActiveScreen(), ActivityScreen.class);
        Container d = utils.getContainer();
        Intent intent = new Intent(utils.getMultitoolContext(), AppChooser.class);
        screen.startActivityForResult(intent, create, d.getId() + "/" + getClass().getName());
    }

    @Override
    public void onActivityResult(int resultCode, Intent data, String token) {
        if (resultCode == Activity.RESULT_OK) {
            Container d = utils.getActiveScreen().getContainerById(Integer.parseInt(token));
            Script resume = utils.installRegisterScript();
            Script pause = utils.installUnregisterScript();
            Shortcut item = d.addShortcut("0", new Intent(), 0, 0);
            Intent intent = data.getParcelableExtra(Intent.EXTRA_INTENT);
            item.setTag(TAG_PACKAGE, intent.getComponent().getPackageName());
            item.getProperties()
                    .edit()
                    .setBoolean(PropertySet.ITEM_ON_GRID, false)
                    .setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false)
                    .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, true)
                    .setBoolean(PropertySet.ITEM_ENABLED, false)
                    .setEventHandler(PropertySet.ITEM_RESUMED, EventHandler.RUN_SCRIPT, resume.getId() + "/" + BadgeListener.class.getName())
                    .setEventHandler(PropertySet.ITEM_PAUSED, EventHandler.RUN_SCRIPT, String.valueOf(pause.getId()))
                    .commit();
            utils.centerOnTouch(item);
        }
    }
}
