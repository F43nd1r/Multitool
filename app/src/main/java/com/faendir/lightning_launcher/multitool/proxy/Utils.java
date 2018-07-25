package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import com.faendir.lightning_launcher.multitool.BuildConfig;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.LightningObjectFactory;
import com.faendir.lightning_launcher.multitool.util.provider.RemoteSharedPreferences;
import java9.util.function.Consumer;
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
    private final LightningObjectFactory.FunctionFactory functionFactory;

    public Utils(LightningObjectFactory.EvalFunction eval, LightningObjectFactory.FunctionFactory functionFactory) {
        this.lightning = ProxyFactory.evalProxy(eval);
        this.functionFactory = functionFactory;
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

    public String getString(@StringRes int res) {
        return multitoolContext.getString(res);
    }

    public String getString(@StringRes int res, Object... formatArgs) {
        return multitoolContext.getString(res, formatArgs);
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

    public Screen getActiveScreen() {
        return lightning.getActiveScreen();
    }

    public Image.Class getImageClass() {
        return Image.Class.get(lightningContext);
    }

    public SharedPreferences getSharedPref() {
        return new RemoteSharedPreferences(lightningContext);
    }

    public Script installNormalScript() {
        return installScript(R.raw.normal, "run");
    }

    public Script installMenuScript() {
        return installScript(R.raw.menu, "menu");
    }

    public Script installActivityResultScript() {
        return installScript(R.raw.activity_result, "activity_result");
    }

    public Script installCreateViewScript() {
        return installScript(R.raw.create_view, "create_view");
    }

    public Script installRegisterScript() {
        return installScript(R.raw.register, "register");
    }

    public Script installUnregisterScript() {
        return installScript(R.raw.unregister, "unregister");
    }

    public Script installCommandScript() {
        return installScript(R.raw.command, "command");
    }

    private Script installScript(@RawRes int res, String name) {
        String path = '/' + BuildConfig.APPLICATION_ID.replace('.', '/');
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

    public Function asFunction(Runnable runnable) {
        return ProxyFactory.lightningProxy(functionFactory.asFunction(runnable), Function.class);
    }

    public Function asFunction(Consumer<?> consumer) {
        return ProxyFactory.lightningProxy(functionFactory.asFunction(consumer), Function.class);
    }

    public void addEventHandler(PropertySet properties, @PropertySet.EventProperty String key, int action, String data) {
        EventHandler eventHandler = EventHandler.newInstance(lightningContext, action, data);
        EventHandler old = properties.getEventHandler(key);
        eventHandler.setNext(old);
        properties.edit().setEventHandler(key, eventHandler).commit();
    }
}
