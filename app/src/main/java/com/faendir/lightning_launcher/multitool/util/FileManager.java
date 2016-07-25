package com.faendir.lightning_launcher.multitool.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Lukas on 04.08.2015.
 * Manages I/O
 */
public class FileManager<T> {

    private final Class<T[]> clazz;
    private final File file;
    private final Gson gson;

    FileManager(Context context, String filename, Class<T[]> clazz) {
        File directory = context.getFilesDir();
        file = new File(directory, filename);
        gson = new GsonBuilder().registerTypeAdapter(Intent.class, new IntentTypeAdapter()).create();
        this.clazz = clazz;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetWorldReadable")
    public static void allowGlobalRead(File file) {
        try {
            if (!file.exists()) file.createNewFile();
            file.setReadable(true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void allowGlobalRead() {
        allowGlobalRead(file);
    }

    @Nullable
    public List<T> read() {
        if (!file.exists()) return null;
        try {
            T[] array = gson.fromJson(new FileReader(file), clazz);
            if (array == null) return null;
            return new ArrayList<>(Arrays.asList(array));
        } catch (FileNotFoundException | JsonSyntaxException e) {
            return null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void write(@NonNull List<T> items) {
        try {
            BufferedWriter writer = null;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                writer = new BufferedWriter(new FileWriter(file));
                gson.toJson(items.toArray(), clazz, writer);
                writer.flush();
            } catch (Exception e) {
                throw new FatalFileException(e);
            } finally {
                if (writer != null) {
                    writer.close();
                }

            }
        } catch (IOException e) {
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
