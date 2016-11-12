package com.faendir.lightning_launcher.multitool.gesture;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Pair;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.scriptlib.PermissionActivity;
import com.faendir.omniadapter.model.DeepObservableList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import java8.util.stream.StreamSupport;

/**
 * Created on 28.01.2016.
 *
 * @author F43nd1r
 */
final class GestureUtils {
    private GestureUtils() {
    }

    static void delete(Context context, List<GestureInfo> selected, List<GestureInfo> list, FileManager<GestureInfo> fileManager) {
        StreamSupport.stream(selected).peek(gestureInfo -> gestureInfo.removeGesture(context)).forEach(list::remove);
        updateSavedGestures(list, fileManager);
    }

    static void edit(Activity context, GestureInfo selected, List<GestureInfo> list) {
        Intent intent = new Intent(context, GestureActivity.class);
        intent.putExtra(GestureActivity.GESTURE, selected);
        intent.putExtra(GestureFragment.INDEX, list.indexOf(selected));
        context.startActivityForResult(intent, GestureFragment.EDIT);

    }

    static void updateSavedGestures(List<GestureInfo> list, FileManager<GestureInfo> fileManager) {
        fileManager.write(new ArrayList<>(list));
    }

    private static final String METADATA = "metadata";
    private static final String GESTURES = "gestures";

    static void exportGestures(final Context context, Uri path, FileManager<GestureInfo> fileManager) {
        final File metadata = fileManager.getFile();
        final File gestures = SingletonGestureLibrary.getFile();
        if (metadata.exists() && gestures.exists()) {
            final File dir = new File(path.getPath());
            final File file = new File(dir, "Multitool_Gestures_" + DateFormat.getDateFormat(context).format(new Date()) + ".zip");
            if ((!dir.mkdirs() && !dir.isDirectory()) || !dir.canWrite() || !file.canWrite()) {
                PermissionActivity.checkForPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, isGranted -> {
                    if (isGranted && (dir.mkdirs() || dir.isDirectory()) && dir.canWrite()) {
                        export(context, file, metadata, gestures);
                    } else {
                        Toast.makeText(context, R.string.toast_failedDirWrite, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                export(context, file, metadata, gestures);
            }
        } else {
            Toast.makeText(context, R.string.toast_noGestures, Toast.LENGTH_SHORT).show();
        }
    }

    private static void export(Context context, File file, File metadata, File gestures) {
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.putNextEntry(new ZipEntry(METADATA));
            writeFile(metadata, out);
            out.putNextEntry(new ZipEntry(GESTURES));
            writeFile(gestures, out);
            Toast.makeText(context, context.getString(R.string.toast_exportedTo, file.getPath()), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.toast_failedExportTo, file.getPath()), Toast.LENGTH_SHORT).show();
        }
    }

    private static void writeFile(File file, OutputStream out) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        }
    }

    private static void readToFile(File file, InputStream in) throws IOException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        }
    }

    static void importGestures(Context context, Uri path, DeepObservableList<GestureInfo> list, FileManager<GestureInfo> fileManager) {
        File file = new File(path.getPath());
        if (file.exists() && file.canRead()) {
            try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                ZipEntry metaEntry = in.getNextEntry();
                if (metaEntry.getName().equals(METADATA)) {
                    File metadata = new File(context.getCacheDir(), METADATA);
                    readToFile(metadata, in);
                    ZipEntry gestureEntry = in.getNextEntry();
                    if (gestureEntry.getName().equals(GESTURES)) {
                        File gestures = new File(context.getCacheDir(), GESTURES);
                        readToFile(gestures, in);
                        in.close();
                        FileManager<GestureInfo> tempFileManager = new FileManager<>(metadata, GestureInfo[].class);
                        List<GestureInfo> gestureInfos = tempFileManager.read();
                        if (!gestureInfos.isEmpty()) {
                            GestureLibrary library = GestureLibraries.fromFile(gestures);
                            library.load();
                            list.beginBatchedUpdates();
                            StreamSupport.stream(gestureInfos).map(info -> new Pair<>(info, library.getGestures(info.getUuid().toString())))
                                    .filter(pair -> !pair.second.isEmpty()).forEach(pair -> {
                                pair.first.setGesture(context, pair.second.get(0));
                                list.add(pair.first);
                            });
                            list.endBatchedUpdates();
                            updateSavedGestures(list, fileManager);
                            Toast.makeText(context, R.string.toast_imported, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }
        Toast.makeText(context, R.string.toast_importFailed, Toast.LENGTH_SHORT).show();
    }
}
