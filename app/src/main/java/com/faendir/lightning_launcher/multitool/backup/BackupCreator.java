package com.faendir.lightning_launcher.multitool.backup;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Keep;
import android.util.Log;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import org.acra.ACRAConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author F43nd1r
 * @since 20.12.2017
 */
@Keep
public class BackupCreator extends AsyncTask<Object, Void, Boolean> implements JavaScript.Direct {
    private static final String CORE = "core";
    private static final String FONTS = "fonts";
    private static final String WALLPAPER = "wallpaper";
    private final WeakReference<Context> contextRef;

    public BackupCreator(Utils utils) {
        this.contextRef = new WeakReference<>(utils.getLightningContext());
    }

    @Override
    protected Boolean doInBackground(Object... voids) {
        Context context = contextRef.get();
        if (context == null) {
            Log.i(MultiTool.LOG_TAG, "Skipped backup because context is not available");
            return false;
        }
        File root = Environment.getExternalStorageDirectory();
        if (root == null) {
            Log.i(MultiTool.LOG_TAG, "Skipped backup because directory is not available");
            return false;
        }
        File directory = new File(root, "LightningLauncher");
        //noinspection ResultOfMethodCallIgnored
        directory.mkdirs();
        File file = new File(directory, "Autobackup.lla");
        try (ZipOutputStream stream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            stream.setMethod(ZipOutputStream.DEFLATED);
            stream.setLevel(0);
            startEntry(stream, "version");
            copy(new ByteArrayInputStream("1".getBytes()), stream);
            stream.closeEntry();
            addDirectoryEntry(stream, CORE);
            File lightning = context.getFilesDir();
            copyDirectory(stream, new File(lightning, "pages"), CORE);
            copyDirectory(stream, new File(lightning, "scripts"), CORE);
            copyDirectory(stream, new File(lightning, "themes"), CORE);
            copyFile(stream, new File(lightning, "config"), CORE);
            copyFile(stream, new File(lightning, "state"), CORE);
            copyFile(stream, new File(lightning, "statistics"), CORE);
            copyFile(stream, new File(lightning, "system"), CORE);
            copyFile(stream, new File(lightning, "variables"), CORE);
            addDirectoryEntry(stream, WALLPAPER);
            Bitmap wallpaper = drawableToBitmap(WallpaperManager.getInstance(context).getDrawable());
            startEntry(stream, WALLPAPER + File.separator + "bitmap.png");
            wallpaper.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.closeEntry();
            addDirectoryEntry(stream, FONTS);
            copyDirectory(stream, new File(lightning, FONTS), "");
            addDirectoryEntry(stream, "widgets_data");
            return true;
        } catch (IOException e) {
            Log.w(MultiTool.LOG_TAG, "Skipped backup because of exception during write", e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Context context = contextRef.get();
        if (context != null) {
            Toast.makeText(context, result ? "Automatic Lightning Backup completed" : "Automatic Lightning Backup failed", Toast.LENGTH_LONG).show();
        }
    }

    private void copyDirectory(ZipOutputStream stream, File directory, String prefix) throws IOException {
        if (directory.exists()) {
            if (prefix.length() > 0) prefix += "/";
            prefix += directory.getName();
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    copyDirectory(stream, file, prefix);
                } else {
                    copyFile(stream, file, prefix);
                }
            }
        }
    }

    private void copyFile(ZipOutputStream stream, File file, String prefix) throws IOException {
        if (file.exists()) {
            startEntry(stream, prefix + "/" + file.getName());
            copy(new FileInputStream(file), stream);
        }
    }

    private void addDirectoryEntry(ZipOutputStream stream, String name) throws IOException {
        startEntry(stream, name + "/");
        stream.closeEntry();

    }

    private void startEntry(ZipOutputStream stream, String name) throws IOException {
        stream.putNextEntry(new ZipEntry(name));
    }

    private void copy(InputStream source, OutputStream target) throws IOException {
        try (InputStream in = new BufferedInputStream(source)) {
            byte[] buffer = new byte[ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES];
            for (int length; (length = in.read(buffer, 0, buffer.length)) > 0; ) {
                target.write(buffer, 0, length);
            }
        }
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public String execute(String data) {
        return String.valueOf(super.execute());
    }
}
