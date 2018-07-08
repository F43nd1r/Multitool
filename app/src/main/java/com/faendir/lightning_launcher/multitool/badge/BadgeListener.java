package com.faendir.lightning_launcher.multitool.badge;

import android.os.Handler;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.Lightning;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import com.faendir.lightning_launcher.multitool.util.provider.BaseContentListener;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
public class BadgeListener extends BaseContentListener {
    private final String packageName;
    private final Utils utils;
    private final Shortcut item;

    public BadgeListener(@NonNull Lightning lightning) {
        super(new Handler(), lightning.getActiveScreen().getContext(), BadgeDataSource.getContentUri(lightning.getEvent().getItem().getTag("package")));
        item = ProxyFactory.cast(lightning.getEvent().getItem(), Shortcut.class);
        utils = new Utils(lightning);
        this.packageName = item.getTag("package");
    }

    @Override
    public void onChange(boolean selfChange) {
        int count = BadgeDataSource.getBadgeCount(getContext(), packageName);
        boolean showZero = utils.getSharedPref().getBoolean(utils.getString(R.string.pref_showZero), true);
        if (!showZero && count == 0) {
            item.setVisibility(false);
        } else {
            item.setLabel(count + "");
            item.setVisibility(true);
        }
    }
}
