package com.faendir.lightning_launcher.multitool.badge

import android.os.Handler
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.JavaScript
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory
import com.faendir.lightning_launcher.multitool.proxy.Shortcut
import com.faendir.lightning_launcher.multitool.proxy.Utils
import com.faendir.lightning_launcher.multitool.util.provider.BaseContentListener

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
class BadgeListener(private val utils: Utils) : BaseContentListener(Handler(), utils.lightningContext, BadgeDataSource.getContentUri(utils.event.item.getTag(BadgeSetup.TAG_PACKAGE))), JavaScript.Listener {
    private val item: Shortcut = ProxyFactory.cast(utils.event.item, Shortcut::class.java)
    private val packageName: String = item.getTag(BadgeSetup.TAG_PACKAGE)

    override fun onChange(selfChange: Boolean) {
        val count = BadgeDataSource.getBadgeCount(context, packageName)
        val showZero = utils.sharedPref.getBoolean(utils.getString(R.string.pref_showZero), true)
        if (!showZero && count == 0) {
            item.setVisibility(false)
        } else {
            item.label = count.toString()
            item.setVisibility(true)
        }
    }
}
