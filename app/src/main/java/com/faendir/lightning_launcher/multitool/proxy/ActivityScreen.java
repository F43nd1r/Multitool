package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Intent;

/**
 * @author lukas
 * @since 08.07.18
 */
public interface ActivityScreen extends Screen {
    boolean startActivityForResult(Intent intent, Script receiver, String token);
}
