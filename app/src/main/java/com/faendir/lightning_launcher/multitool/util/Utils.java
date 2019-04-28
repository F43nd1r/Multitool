package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.acra.util.StreamReader;

import java.io.IOException;

/**
 * @author F43nd1r
 * @since 09.11.2016
 */

public final class Utils {
    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Intent.class, new IntentTypeAdapter())
            .registerTypeAdapter(ParcelUuid.class, new ParcelUuidTypeAdapter())
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

    public static class ParcelUuidTypeAdapter extends TypeAdapter<ParcelUuid> {
        @Override
        public void write(JsonWriter out, ParcelUuid value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public ParcelUuid read(JsonReader in) throws IOException {
            return ParcelUuid.fromString(in.nextString());
        }
    }

    public static String readRawResource(@NonNull Context context, @RawRes int res) {
        try {
            return new StreamReader(context.getResources().openRawResource(res)).read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
