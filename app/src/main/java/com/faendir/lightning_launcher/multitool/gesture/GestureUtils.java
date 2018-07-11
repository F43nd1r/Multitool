package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.gesture.GestureStore;
import android.net.Uri;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created on 28.01.2016.
 *
 * @author F43nd1r
 */
final class GestureUtils {
    private GestureUtils() {
    }

    static void delete(Context context, GestureInfo selected, List<GestureInfo> list) {
        selected.removeGesture(context);
        list.remove(selected);
        writeToFile(context, list);
    }

    private static final String METADATA = "metadata";
    private static final String GESTURES = "gestures";

    static void exportGestures(final Context context, Uri path) {
        try {
            final InputStream metadata = DataProvider.openFileForRead(context, GestureMetaDataSource.class);
            final InputStream gestures = DataProvider.openFileForRead(context, GestureLibraryDataSource.class);
            final OutputStream outputStream = context.getContentResolver().openOutputStream(path);
            if (outputStream != null) {
                try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream))) {
                    out.putNextEntry(new ZipEntry(METADATA));
                    writeAndClose(metadata, out);
                    out.putNextEntry(new ZipEntry(GESTURES));
                    writeAndClose(gestures, out);
                    Toast.makeText(context, context.getString(R.string.toast_exportedTo, path.toString()), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(context, context.getString(R.string.toast_failedExportTo, path.toString()), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, R.string.toast_fileNotWritable, Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(context, R.string.toast_noGestures, Toast.LENGTH_SHORT).show();
        }
    }

    private static void writeAndClose(InputStream input, OutputStream out) throws IOException {
        try (InputStream in = new BufferedInputStream(input)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        }
    }

    static void importGestures(Context context, Uri path, List<GestureInfo> list) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(path);
            if (inputStream == null) {
                throw new FileNotFoundException();
            }
            ZipInputStream in = new ZipInputStream(inputStream);
            ZipEntry entry;
            GestureStore library = null;
            List<GestureInfo> gestureInfos = null;
            while ((entry = in.getNextEntry()) != null) {
                switch (entry.getName()) {
                    case METADATA:
                        gestureInfos = Arrays.asList(Utils.GSON.fromJson(new InputStreamReader(in), GestureInfo[].class));
                        break;
                    case GESTURES:
                        library = new GestureStore();
                        library.load(in, false);
                        break;
                    default:
                        throw new IOException();
                }
                in.closeEntry();
            }
            in.close();
            if (library == null || gestureInfos == null) {
                throw new IOException();
            }
            if (!gestureInfos.isEmpty()) {
                for (GestureInfo info : gestureInfos) {
                    info.setGesture(context, library.getGestures(info.getUuid().toString()).get(0));
                    list.add(info);
                }
                writeToFile(context, list);
                Toast.makeText(context, R.string.toast_imported, Toast.LENGTH_SHORT).show();

            }

        } catch (IOException e) {
            Toast.makeText(context, R.string.toast_importFailed, Toast.LENGTH_SHORT).show();
        }
    }

    static List<GestureInfo> readFromFile(Context context) {
        try (InputStreamReader reader = new InputStreamReader(DataProvider.openFileForRead(context, GestureMetaDataSource.class))) {
            GestureInfo[] array = Utils.GSON.fromJson(reader, GestureInfo[].class);
            if (array != null) {
                return new ArrayList<>(Arrays.asList(array));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    static void writeToFile(Context context, List<GestureInfo> infos) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(DataProvider.openFileForWrite(context, GestureMetaDataSource.class)))) {
            Utils.GSON.toJson(infos.toArray(), GestureInfo[].class, writer);
            writer.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
