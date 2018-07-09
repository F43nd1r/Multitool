package com.faendir.lightning_launcher.multitool.util;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    public void init(EvalFunction eval) {
        this.utils = new Utils(eval);
    }

    public JavaScript get(String className) {
        try {
            //noinspection unchecked
            Class<? extends JavaScript> clazz = (Class<? extends JavaScript>) Class.forName(className);
            return clazz.getConstructor(Utils.class).newInstance(utils);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface EvalFunction {
        Object eval(@Nullable Object target, @NonNull String methodName, Object... parameters);
    }
}
