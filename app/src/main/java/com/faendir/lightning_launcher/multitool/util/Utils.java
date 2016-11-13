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
 * Created by Lukas on 09.11.2016.
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
                    result.add(clip.getItemAt(i).getUri());
                }
            } else {
                List<String> paths = data.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);
                if (paths != null) {
                    result.addAll(StreamSupport.stream(paths).map(Uri::parse).collect(Collectors.toList()));
                }
            }
        } else {
            result.add(data.getData());
        }
        return result;
    }
}
