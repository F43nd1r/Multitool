// IBadgeService.aidl
package com.faendir.lightning_launcher.multitool.badge;

// Declare any non-default types here with import statements
import com.faendir.lightning_launcher.multitool.badge.IBadgeListener;

interface IBadgeService {
    void registerListener(IBadgeListener listener);
    void unregisterListener(IBadgeListener listener);
    void publish(int count, String packageName);
}
