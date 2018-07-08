package com.faendir.lightning_launcher.multitool.util;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * @author F43nd1r
 * @since 09.11.2016
 */

public final class Utils {
    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Intent.class, new IntentTypeAdapter())
            .create();

    private Utils() {
    }

    public static class IntentTypeAdapter extends TypeAdapter<Intent> {
        private static final String URI = "uri";

        @Override
        public void write(JsonWriter out, Intent value) throws IOException {
            out.beginObject();
            out.name(URI);
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toUri(0));
            }
            out.endObject();
        }

        @Override
        public Intent read(JsonReader in) throws IOException {
            Intent intent = null;
            in.beginObject();
            while (in.hasNext()) {
                if (URI.equals(in.nextName())) {
                    intent = LambdaUtils.exceptionToOptional(Intent::parseUri).apply(in.nextString(), 0).orElse(intent);
                }
            }
            in.endObject();
            return intent;
        }
    }
}
