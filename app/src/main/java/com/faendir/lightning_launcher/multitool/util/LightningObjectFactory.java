package com.faendir.lightning_launcher.multitool.util;

import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

/**
 * @author F43nd1r
 * @since 07.11.2017
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Keep
public class LightningObjectFactory {
    private Utils utils;

    /**
     * Reflection constructor. Call init after calling this
     */
    public LightningObjectFactory() {
    }

    public LightningObjectFactory(Utils utils) {
        this.utils = utils;
    }

    public void init(EvalFunction eval, FunctionFactory asFunction) {
        this.utils = new Utils(eval, asFunction);
    }

    public JavaScript get(String className) {
        try {
            if (MultiTool.DEBUG) Log.d(MultiTool.LOG_TAG, "ObjectFactory loading " + className);
            //noinspection unchecked
            Class<? extends JavaScript> clazz = (Class<? extends JavaScript>) Class.forName(className);
            return clazz.getConstructor(Utils.class).newInstance(utils);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface EvalFunction {
        Object eval(@NonNull String methodName, Object... parameters);
    }

    @FunctionalInterface
    public interface FunctionFactory {
        Object asFunction(@NonNull Object target);
    }
}
