package com.faendir.lightning_launcher.multitool.util;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

/**
 * @author lukas
 * @since 09.07.18
 */
public class SetupDistributor implements JavaScript.Setup {
    private final Utils utils;

    public SetupDistributor(Utils utils) {
        this.utils = utils;
    }

    @Override
    public void setup() {
        String className = utils.getSharedPref().getString(utils.getString(R.string.pref_setupClass), null);
        if (className != null) {
            utils.getSharedPref().edit().putString(utils.getString(R.string.pref_setupClass), null).apply();
            ((Setup) new LightningObjectFactory(utils).get(className)).setup();
        }
    }
}
