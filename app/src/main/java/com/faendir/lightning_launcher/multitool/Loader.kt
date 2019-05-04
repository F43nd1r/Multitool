package com.faendir.lightning_launcher.multitool

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.core.content.pm.PackageInfoCompat
import com.faendir.lightning_launcher.multitool.MultiTool.Companion.DEBUG
import com.faendir.lightning_launcher.multitool.MultiTool.Companion.LOG_TAG
import com.faendir.lightning_launcher.multitool.animation.AnimationScript
import com.faendir.lightning_launcher.multitool.badge.BadgeSetup
import com.faendir.lightning_launcher.multitool.billing.BaseBillingManager
import com.faendir.lightning_launcher.multitool.billing.BillingManager
import com.faendir.lightning_launcher.multitool.calendar.CalendarScript
import com.faendir.lightning_launcher.multitool.drawer.Drawer
import com.faendir.lightning_launcher.multitool.gesture.GestureScript
import com.faendir.lightning_launcher.multitool.immersive.ImmersiveScript
import com.faendir.lightning_launcher.multitool.music.MusicSetup
import com.faendir.lightning_launcher.multitool.proxy.JavaScript

/**
 * @author F43nd1r
 * @since 26.10.2016
 */

class Loader : Activity() {

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingManager = BillingManager(this)
        if (DEBUG) Log.d(LOG_TAG, "Loader for class " + intent.component!!.className)
        when (intent.component!!.className) {
            LAUNCHER_SCRIPT -> check(null, R.raw.multitool, false, FLAG_APP_MENU + FLAG_ITEM_MENU, getString(R.string.script_name), true)
            GESTURE_LAUNCHER -> setupCheck<GestureScript>(null)
            MUSIC_WIDGET -> setupCheck<MusicSetup>(BaseBillingManager.PaidFeature.MUSIC_WIDGET)
            DRAWER -> setupCheck<Drawer>(BaseBillingManager.PaidFeature.DRAWER)
            IMMERSIVE -> setupCheck<ImmersiveScript>(null)
            ANIMATION -> setupCheck<AnimationScript>(BaseBillingManager.PaidFeature.ANIMATION)
            BADGE -> setupCheck<BadgeSetup>(null)
            CALENDAR -> setupCheck<CalendarScript>(null)
        }
    }

    private inline fun <reified T : JavaScript.Setup> setupCheck(feature: BaseBillingManager.PaidFeature?) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(getString(R.string.pref_setupClass), T::class.java.name).apply()
        check(feature, R.raw.setup, true, 0, null, true)
    }

    private fun check(feature: BaseBillingManager.PaidFeature?, @RawRes script: Int, runAndDelete: Boolean, flags: Int, name: String?, showDialog: Boolean) {
        if (checkLightningVersion()) {
            if (feature != null) {
                Thread {
                    when {
                        billingManager.isBoughtOrTrial(feature) -> setResult(script, runAndDelete, flags, name)
                        showDialog -> runOnUiThread { billingManager.showTrialBuyDialog(feature) { check(feature, script, runAndDelete, flags, name, false) } }
                        else -> {
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                    }
                }.start()
            } else {
                setResult(script, runAndDelete, flags, name)
            }
        } else {
            Toast.makeText(this, R.string.toast_launcherOutdated, Toast.LENGTH_LONG).show()
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun setResult(@RawRes script: Int, runAndDelete: Boolean, flags: Int, name: String?) {
        val intent = Intent()
        intent.putExtra(INTENT_EXTRA_SCRIPT_ID, script)
        intent.putExtra(INTENT_EXTRA_SCRIPT_NAME, name)
        intent.putExtra(INTENT_EXTRA_SCRIPT_PACKAGE, BuildConfig.APPLICATION_ID.replace('.', '/'))
        intent.putExtra(INTENT_EXTRA_SCRIPT_FLAGS, flags)
        intent.putExtra(INTENT_EXTRA_EXECUTE_ON_LOAD, runAndDelete)
        intent.putExtra(INTENT_EXTRA_DELETE_AFTER_EXECUTION, runAndDelete)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onDestroy() {
        billingManager.release()
        super.onDestroy()
    }

    private fun checkLightningVersion(): Boolean {
        return (packageManager.getPackageInfo("net.pierrox.lightning_launcher_extreme", 0)
                ?: packageManager.getPackageInfo("net.pierrox.lightning_launcher", 0))?.let { info -> PackageInfoCompat.getLongVersionCode(info) % 1000 >= 307 } ?: false
    }

    companion object {
        private const val LAUNCHER_SCRIPT = BuildConfig.APPLICATION_ID + ".LoadLauncherScript"
        private const val GESTURE_LAUNCHER = BuildConfig.APPLICATION_ID + ".LoadGestureLauncher"
        private const val MUSIC_WIDGET = BuildConfig.APPLICATION_ID + ".LoadMusicWidget"
        private const val DRAWER = BuildConfig.APPLICATION_ID + ".LoadDrawer"
        private const val IMMERSIVE = BuildConfig.APPLICATION_ID + ".toggleImmersive"
        private const val ANIMATION = BuildConfig.APPLICATION_ID + ".LoadAnimation"
        private const val BADGE = BuildConfig.APPLICATION_ID + ".LoadBadge"
        private const val CALENDAR = BuildConfig.APPLICATION_ID + ".LoadCalendar"

        const val FLAG_DISABLED = 1
        const val FLAG_APP_MENU = 2
        const val FLAG_ITEM_MENU = 4
        const val FLAG_CUSTOM_MENU = 8
        private const val INTENT_EXTRA_SCRIPT_ID = "i"
        private const val INTENT_EXTRA_SCRIPT_FLAGS = "f"
        private const val INTENT_EXTRA_SCRIPT_NAME = "n"
        private const val INTENT_EXTRA_EXECUTE_ON_LOAD = "e"
        private const val INTENT_EXTRA_DELETE_AFTER_EXECUTION = "d"
        private const val INTENT_EXTRA_SCRIPT_PACKAGE = "p"
    }
}
