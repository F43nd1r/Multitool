package com.faendir.lightning_launcher.multitool.gesture;

import android.Manifest;
import android.content.Context;
import android.gesture.GestureStore;
import android.net.Uri;
import android.util.Pair;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.scriptlib.PermissionActivity;

import org.acra.ACRA;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import java8.util.stream.StreamSupport;

import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.ignoreExceptions;

/**
 * Created on 28.01.2016.
 *
 * @author F43nd1r
 */
final class GestureUtils {
    private GestureUtils() {
    }

    static void delete(Context context, GestureInfo selected, List<GestureInfo> list, FileManager<GestureInfo, FileNotFoundException> fileManager) {
        selected.removeGesture(context);
        list.remove(selected);
        updateSavedGestures(list, fileManager);
    }

    static void updateSavedGestures(List<GestureInfo> list, FileManager<GestureInfo, FileNotFoundException> fileManager) {
        try {
            fileManager.write(list);
        } catch (IOException e) {
            ACRA.getErrorReporter().handleSilentException(e);
        }
    }

    private static final String METADATA = "metadata";
    private static final String GESTURES = "gestures";

    static void exportGestures(final Context context, Uri path) {
        try {
            final InputStream metadata = DataProvider.openFileForRead(context, GestureMetaDataSource.class);
            final InputStream gestures = DataProvider.openFileForRead(context, GestureLibraryDataSource.class);
            final File dir = new File(path.getPath());
            final File file = new File(dir, "Multitool_Gestures_" + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ".zip");
            if ((!dir.mkdirs() && !dir.isDirectory()) || !dir.canWrite() || !file.canWrite()) {
                PermissionActivity.checkForPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, isGranted -> {
                    if (isGranted && (dir.mkdirs() || dir.isDirectory()) && dir.canWrite()) {
                        exportAndClose(context, file, metadata, gestures);
                    } else {
                        Toast.makeText(context, R.string.toast_failedDirWrite, Toast.LENGTH_SHORT).show();
                        ignoreExceptions(metadata::close).run();
                        ignoreExceptions(gestures::close).run();
                    }
                });
            } else {
                exportAndClose(context, file, metadata, gestures);
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(context, R.string.toast_noGestures, Toast.LENGTH_SHORT).show();
        }
    }

    private static void exportAndClose(Context context, File file, InputStream metadata, InputStream gestures) {
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.putNextEntry(new ZipEntry(METADATA));
            writeAndClose(metadata, out);
            out.putNextEntry(new ZipEntry(GESTURES));
            writeAndClose(gestures, out);
            Toast.makeText(context, context.getString(R.string.toast_exportedTo, file.getPath()), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.toast_failedExportTo, file.getPath()), Toast.LENGTH_SHORT).show();
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

    static void importGestures(Context context, Uri path, List<GestureInfo> list, FileManager<GestureInfo, FileNotFoundException> fileManager) {
        File file = new File(path.getPath());
        if (file.exists() && file.canRead()) {
            try (ZipFile in = new ZipFile(file)) {
                ZipEntry metaEntry = in.getEntry(METADATA);
                ZipEntry gestureEntry = in.getEntry(GESTURES);
                if (metaEntry != null && gestureEntry != null) {
                    if (copyGestures(context, list, fileManager, in.getInputStream(gestureEntry), in.getInputStream(metaEntry))) {
                        Toast.makeText(context, R.string.toast_imported, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } catch (IOException ignored) {
            }
        }
        Toast.makeText(context, R.string.toast_importFailed, Toast.LENGTH_SHORT).show();
    }

    private static boolean copyGestures(Context context, List<GestureInfo> list, FileManager<GestureInfo, FileNotFoundException> fileManager, InputStream gestures, InputStream metadata) {
        FileManager<GestureInfo, FileNotFoundException> tempFM = new FileManager<>(() -> metadata, () -> {
            throw new UnsupportedOperationException();
        }, GestureInfo[].class);
        try {
            List<GestureInfo> gestureInfos = tempFM.read();
            if (!gestureInfos.isEmpty()) {
                GestureStore library = new GestureStore();
                library.load(gestures, true);
                StreamSupport.stream(gestureInfos).map(info -> new Pair<>(info, library.getGestures(info.getUuid().toString()))).filter(pair -> !pair.second.isEmpty()).forEach(pair -> {
                    pair.first.setGesture(context, pair.second.get(0));
                    list.add(pair.first);
                });
                updateSavedGestures(list, fileManager);
                return true;
            }
        } catch (IOException ignored) {
        }
        return false;
    }
}
