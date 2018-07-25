package com.faendir.lightning_launcher.multitool.badge;

import android.os.Handler;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import com.faendir.lightning_launcher.multitool.util.provider.BaseContentListener;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
public class BadgeListener extends BaseContentListener implements JavaScript.Listener {
    private final String packageName;
    private final Utils utils;
    private final Shortcut item;

    public BadgeListener(@NonNull Utils utils) {
        super(new Handler(), utils.getLightningContext(), BadgeDataSource.getContentUri(utils.getEvent().getItem().getTag(BadgeSetup.TAG_PACKAGE)));
        this.utils = utils;
        item = ProxyFactory.cast(utils.getEvent().getItem(), Shortcut.class);
        this.packageName = item.getTag(BadgeSetup.TAG_PACKAGE);
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
