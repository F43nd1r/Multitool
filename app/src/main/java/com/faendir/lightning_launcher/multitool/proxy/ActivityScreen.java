package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Intent;

/**
 * @author lukas
 * @since 08.07.18
 */
public interface ActivityScreen extends Screen {
    ImageBitmap cropImage(ImageBitmap image, boolean full_size);

    void hideActionBar();

    int pickColor(String title, int color, boolean hasAlpha);

    Image pickImage(int maxPixels);

    float pickNumericValue(String title, float value, String valueType, float min, float max, float interval, String unit);

    void showActionBar(Function onCreateOptionsMenu, Function onOptionsItemSelected);

    boolean startActivityForResult(Intent intent, Script receiver, String token);
}
