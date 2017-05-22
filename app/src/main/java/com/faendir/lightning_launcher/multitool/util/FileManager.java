package com.faendir.lightning_launcher.multitool.util;

import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lukas on 04.08.2015.
 * Manages I/O
 */
public class FileManager<T, E extends Throwable> {

    private final Class<T[]> clazz;
    private final Gson gson;
    private final LambdaUtils.ExceptionalSupplier<ParcelFileDescriptor, E> fileSupplier;

    public FileManager(LambdaUtils.ExceptionalSupplier<ParcelFileDescriptor, E> fileSupplier, Class<T[]> clazz) {
        this.fileSupplier = fileSupplier;
        gson = new GsonBuilder()
                .registerTypeAdapter(Intent.class, new IntentTypeAdapter())
                .create();
        this.clazz = clazz;
    }

    @NonNull
    public List<T> read() throws E {
        ParcelFileDescriptor file = fileSupplier.get();
        if (file.getFileDescriptor().valid()) {
            try (InputStreamReader reader = new InputStreamReader(new ParcelFileDescriptor.AutoCloseInputStream(file))) {
                T[] array = gson.fromJson(reader, clazz);
                if (array != null) {
                    return new ArrayList<>(Arrays.asList(array));
                }
            } catch (Throwable ignored) {
            }
        }
        return Collections.emptyList();
    }

    public void write(@NonNull List<T> items) throws E {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new ParcelFileDescriptor.AutoCloseOutputStream(fileSupplier.get())))) {
            gson.toJson(items.toArray(), clazz, writer);
            writer.flush();
        } catch (Exception e) {
            throw new FatalFileException(e);
        }
    }

    public static class FatalFileException extends RuntimeException {
        public FatalFileException(Exception e) {
            super(e);
        }
    }

    private static class IntentTypeAdapter extends TypeAdapter<Intent> {
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
