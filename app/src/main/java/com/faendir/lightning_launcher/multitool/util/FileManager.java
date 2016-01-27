package com.faendir.lightning_launcher.multitool.util;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
        gson = new Gson();
        this.clazz = clazz;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetWorldReadable")
    public void allowGlobalRead() {
        try {
            if (!file.exists()) file.createNewFile();
            file.setReadable(true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<T> read() {
        if (!file.exists()) return null;
        try {
            T[] array = gson.fromJson(new FileReader(file), clazz);
            if (array == null) return null;
            return Arrays.asList(array);
        } catch (FileNotFoundException | JsonSyntaxException e) {
            return null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void write(List<T> items) {
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
}
