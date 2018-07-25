package com.faendir.lightning_launcher.multitool.proxy;

import androidx.annotation.Keep;

/**
 * @author lukas
 * @since 04.07.18
 */
@Keep
public interface Proxy {
    Object getReal();
}
