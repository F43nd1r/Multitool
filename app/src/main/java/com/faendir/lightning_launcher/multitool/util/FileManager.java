package com.faendir.lightning_launcher.multitool.util;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java8.util.function.Supplier;

/**
 * Created by Lukas on 04.08.2015.
 * Manages I/O
 */
public class FileManager<T> {

    private final Class<T[]> clazz;
    private final Gson gson;
    private final Supplier<FileDescriptor> fileDescriptorSupplier;

    public FileManager(Supplier<FileDescriptor> fileDescriptorSupplier, Class<T[]> clazz) {
        this.fileDescriptorSupplier = fileDescriptorSupplier;
        gson = new GsonBuilder()
                .registerTypeAdapter(Intent.class, new IntentTypeAdapter())
                .create();
        this.clazz = clazz;
    }

    @NonNull
    public List<T> read() {
        FileDescriptor file = fileDescriptorSupplier.get();
        if (file.valid()) {
            try (FileReader reader = new FileReader(file)) {
                T[] array = gson.fromJson(reader, clazz);
                if (array != null) {
                    return new ArrayList<>(Arrays.asList(array));
                }
            } catch (Throwable ignored) {
            }
        }
        return Collections.emptyList();
    }

    public void write(@NonNull List<T> items) {
        try (Writer writer = new BufferedWriter(new FileWriter(fileDescriptorSupplier.get()))) {
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
                    try {
                        intent = Intent.parseUri(in.nextString(), 0);
                    } catch (URISyntaxException ignored) {
                    }
                }
            }
            in.endObject();
            return intent;
        }
    }

}
