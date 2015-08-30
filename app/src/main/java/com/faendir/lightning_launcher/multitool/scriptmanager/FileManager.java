package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Lukas on 04.08.2015.
 * Caches Pages on the filesystem, manages loading and unloading
 */
class FileManager {

    private static final Type TYPE = new TypeToken<List<ScriptGroup>>() {
    }.getType();

    private final File file;
    private final Gson gson;

    public FileManager(Context context) {
        File directory = context.getFilesDir();
        file = new File(directory, "storage");
        gson = new Gson();
    }

    public List<ScriptGroup> read() {
        if (!file.exists()) return null;
        try {
            return gson.fromJson(new FileReader(file), TYPE);
        } catch (FileNotFoundException e) {
            throw new FatalFileException(e);
        }
    }

    public void write(List<ScriptGroup> items) {
        try {
            BufferedWriter writer = null;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                writer = new BufferedWriter(new FileWriter(file));
                gson.toJson(items, TYPE, writer);
                writer.flush();
            } catch (IOException e) {
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
}
