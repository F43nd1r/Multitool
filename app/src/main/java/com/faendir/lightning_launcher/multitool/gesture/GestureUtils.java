package com.faendir.lightning_launcher.multitool.gesture;

import android.Manifest;
import android.content.Context;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Pair;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.DataProvider;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.scriptlib.PermissionActivity;
import com.faendir.omniadapter.model.DeepObservableList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    static void updateSavedGestures(List<GestureInfo> list, FileManager<GestureInfo> fileManager) {
        fileManager.write(new ArrayList<>(list));
    }

    private static final String METADATA = "metadata";
    private static final String GESTURES = "gestures";

    static void exportGestures(final Context context, Uri path) {
        final FileDescriptor metadata = DataProvider.getGestureInfoFile(context);
        final FileDescriptor gestures = DataProvider.getGestureLibraryFile(context);
        if (metadata.valid() && gestures.valid()) {
            final File dir = new File(path.getPath());
            final File file = new File(dir, "Multitool_Gestures_" + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ".zip");
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

    private static void export(Context context, File file, FileDescriptor metadata, FileDescriptor gestures) {
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

    private static void writeFile(FileDescriptor file, OutputStream out) throws IOException {
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
                if (metaEntry != null && metaEntry.getName().equals(METADATA)) {
                    File metadata = new File(context.getCacheDir(), METADATA);
                    readToFile(metadata, in);
                    ZipEntry gestureEntry = in.getNextEntry();
                    if (gestureEntry != null && gestureEntry.getName().equals(GESTURES)) {
                        File gestures = new File(context.getCacheDir(), GESTURES);
                        readToFile(gestures, in);
                        in.close();
                        FileManager<GestureInfo> tempFileManager = new FileManager<>(
                                () -> {
                                    try {
                                        return ParcelFileDescriptor.open(metadata, ParcelFileDescriptor.MODE_READ_ONLY).getFileDescriptor();
                                    } catch (FileNotFoundException e) {
                                        return new FileDescriptor();
                                    }
                                }, GestureInfo[].class);
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
