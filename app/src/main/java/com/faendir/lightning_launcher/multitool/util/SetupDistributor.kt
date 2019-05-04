package com.faendir.lightning_launcher.multitool.util

import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.JavaScript
import com.faendir.lightning_launcher.multitool.proxy.Utils

/**
 * @author lukas
 * @since 09.07.18
 */
class SetupDistributor(private val utils: Utils) : JavaScript.Setup {
    override fun setup() {
        val className = utils.sharedPref.getString(utils.getString(R.string.pref_setupClass), null)
        if (className != null) {
            utils.sharedPref.edit().putString(utils.getString(R.string.pref_setupClass), null).apply()
            (LightningObjectFactory(utils)[className] as JavaScript.Setup).setup()
        }
    }
}
