package com.faendir.lightning_launcher.multitool.immersive;

import android.app.Activity;
import android.os.Handler;
import androidx.annotation.Keep;
import android.view.View;
import com.faendir.lightning_launcher.multitool.proxy.Desktop;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

/**
 * @author lukas
 * @since 09.07.18
 */
@Keep
public class ImmersiveScript implements JavaScript.Normal, JavaScript.Setup {
    private final Utils utils;

    public ImmersiveScript(Utils utils) {
        this.utils = utils;
    }

    @Override
    public void setup() {
        Script script = utils.installNormalScript();
        Desktop desktop = utils.getActiveScreen().getCurrentDesktop();
        PropertySet properties = desktop.getProperties();
        EventHandler eventHandler = properties.getEventHandler(PropertySet.RESUMED);
        if (eventHandler.getAction() == EventHandler.RUN_SCRIPT && eventHandler.getData() != null && eventHandler.getData().startsWith(String.valueOf(script.getId()))) {
            properties.edit().setEventHandler(PropertySet.RESUMED, EventHandler.UNSET, null).commit();
            ((Activity) utils.getLightningContext()).getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            properties.edit().setEventHandler(PropertySet.RESUMED, EventHandler.RUN_SCRIPT, script.getId() + "/" + getClass().getName()).commit();
            script.run(utils.getActiveScreen(), null);
        }
    }

    @Override
    public void run() {
        new Handler(utils.getLightningContext().getMainLooper()).post(() -> ((Activity) utils.getLightningContext()).getWindow()
                .getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN));
    }
}
