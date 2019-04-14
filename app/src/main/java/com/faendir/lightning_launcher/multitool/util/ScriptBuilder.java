package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import androidx.annotation.NonNull;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;

/**
 * @author lukas
 * @since 09.02.19
 */
public class ScriptBuilder {
    private final StringBuilder builder = new StringBuilder();

    public ScriptBuilder addVariable(@NonNull String name, @NonNull String value) {
        builder.append("var ").append(name).append(" = ").append(value).append(";\n");
        return this;
    }

    public ScriptBuilder loadLibrary(@NonNull Context context) {
        builder.append(Utils.readRawResource(context, R.raw.library)).append("\n");
        return this;
    }

    public ScriptBuilder addStatement(@NonNull String statement) {
        builder.append(statement).append("\n");
        return this;
    }

    @NonNull
    public String toString() {
        return builder.toString();
    }


    public static String scriptForClass(@NonNull Context context, @NonNull Class<? extends JavaScript.Direct> clazz) {
        return new ScriptBuilder()
                .loadLibrary(context)
                .addStatement("return getObjectFactory().get('" + clazz.getCanonicalName() + "').execute(null);")
                .toString();
    }
}
