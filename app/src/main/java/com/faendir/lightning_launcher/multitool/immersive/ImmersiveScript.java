package com.faendir.lightning_launcher.multitool.immersive;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.Keep;
import android.view.View;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.Desktop;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

/**
 * @author lukas
 * @since 09.07.18
 */
@Keep
public class ImmersiveScript {
    private final Utils utils;

    public ImmersiveScript(Utils utils) {
        this.utils = utils;
    }

    public void setup() {
        Script script = utils.installScript("immersive", R.raw.immersive, "Toggle immersive mode");
        Desktop desktop = utils.getActiveScreen().getCurrentDesktop();
        PropertySet properties = desktop.getProperties();
        EventHandler eventHandler = properties.getEventHandler("resumed");
        if (eventHandler.getAction() == EventHandler.RUN_SCRIPT && String.valueOf(script.getId()).equals(eventHandler.getData())) {
            properties.edit().setEventHandler("resumed", EventHandler.UNSET, null).commit();
            ((Activity) utils.getLightningContext()).getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            properties.edit().setEventHandler("resumed", EventHandler.RUN_SCRIPT, String.valueOf(script.getId())).commit();
            script.run(utils.getActiveScreen(), null);
        }
    }

    public void run() {
        new Handler(utils.getLightningContext().getMainLooper()).post(() -> ((Activity) utils.getLightningContext()).getWindow()
                .getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN));
    }
}
