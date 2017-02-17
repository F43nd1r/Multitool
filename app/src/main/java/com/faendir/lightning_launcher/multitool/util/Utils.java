package com.faendir.lightning_launcher.multitool.util;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.google.gson.Gson;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * @author F43nd1r
 * @since 09.11.2016
 */

public final class Utils {
    public static final Gson GSON = new Gson();

    private Utils() {
    }

    @SuppressWarnings("unused")
    public static List<Uri> getFilePickerActivityResult(Intent data) {
        List<Uri> result = new ArrayList<>();
        if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
            ClipData clip;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && (clip = data.getClipData()) != null) {
                for (int i = 0; i < clip.getItemCount(); i++) {
                    Uri uri = clip.getItemAt(i).getUri();
                    if (uri != null) {
                        result.add(uri);
                    }
                }
            } else {
                List<String> paths = data.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);
                if (paths != null) {
                    result.addAll(StreamSupport.stream(paths).filter(path -> path != null).map(Uri::parse).collect(Collectors.toList()));
                }
            }
        } else {
            Uri uri = data.getData();
            if (uri != null) {
                result.add(uri);
            }
        }
        return result;
    }
}
