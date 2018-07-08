package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.RawRes;
import com.faendir.lightning_launcher.multitool.BuildConfig;
import org.acra.util.StreamReader;

import java.io.IOException;

/**
 * @author lukas
 * @since 05.07.18
 */
public class Utils {
    private final Context lightningContext;
    private final Context multitoolContext;
    private final Lightning lightning;

    public Utils(Lightning lightning) {
        this.lightning = lightning;
        this.lightningContext = lightning.getActiveScreen().getContext();
        try {
            this.multitoolContext = lightningContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Context getLightningContext() {
        return lightningContext;
    }

    public Context getMultitoolContext() {
        return multitoolContext;
    }

    public Resources getMultitoolResources() {
        return multitoolContext.getResources();
    }

    public Lightning getLightning() {
        return lightning;
    }

    public Event getEvent() {
        return lightning.getEvent();
    }

    public Container getContainer() {
        return getEvent().getContainer();
    }

    public Image.Class getImageClass() {
        return Image.Class.get(lightningContext);
    }

    public Script installScript(String pathSuffix, @RawRes int res, String name) {
        String path = '/' + BuildConfig.APPLICATION_ID.replace('.', '/') + '/' + pathSuffix;
        Script script = lightning.getScriptByPathAndName(path, name);
        try {
            String script_text = new StreamReader(getMultitoolResources().openRawResource(res)).read();
            if (script == null) {
                script = lightning.createScript(path, name, script_text, 0);
            } else {
                script.setText(script_text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return script;
    }

    public void centerOnTouch(Item item) {
        Screen screen = lightning.getActiveScreen();
        // use the last screen touch position, if any, as location for the new item
        float x = screen.getLastTouchX();
        float y = screen.getLastTouchY();
        float width = item.getWidth();
        float height = item.getHeight();
        if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
            // no previous touch event, use a default position (can happen when using the hardware menu key for instance)
            x = width;
            y = height;
        } else {
            // center around the touch position
            x -= width / 2;
            y -= height / 2;
        }
        item.setPosition(x, y);
    }
}
