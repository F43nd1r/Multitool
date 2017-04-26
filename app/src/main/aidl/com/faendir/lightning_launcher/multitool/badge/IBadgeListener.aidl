// IBadgeListener.aidl
package com.faendir.lightning_launcher.multitool.badge;

// Declare any non-default types here with import statements

interface IBadgeListener {
    void onCountChange(int newCount, String packageName);
}
