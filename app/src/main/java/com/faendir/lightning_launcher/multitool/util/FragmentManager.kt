package com.faendir.lightning_launcher.multitool.util

import android.content.SharedPreferences
import android.content.res.Resources
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import com.faendir.lightning_launcher.multitool.MainActivity
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.billing.BaseBillingManager
import com.faendir.lightning_launcher.multitool.billing.BillingManager
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest
import com.mikepenz.materialdrawer.Drawer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created on 01.04.2016.
 *
 * @author F43nd1r
 */
class FragmentManager(private val context: MainActivity, private val billingManager: BillingManager, private val drawer: Drawer) {
    private val manager: androidx.fragment.app.FragmentManager = context.supportFragmentManager
    private val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var currentFragment: Fragment? = null
    private var lastId: Int = 0

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onSwitchFragmentRequest(request: SwitchFragmentRequest) {
        val name: String = try {
            context.resources.getResourceName(request.id)
        } catch (e: Resources.NotFoundException) {
            "none"
        }

        if (currentFragment != null && sharedPref.getString(context.getString(R.string.pref_lastFragment), "") == name) {
            return
        }
        val feature = BaseBillingManager.PaidFeature.fromFragment(request.fragment)
        if (feature !=null && !billingManager.isBoughtOrTrial(feature)) {
            context.runOnUiThread {
                billingManager.showTrialBuyDialog(feature)
                drawer.setSelection(lastId.toLong())
            }
        } else {
            context.runOnUiThread {
                currentFragment = request.fragment.newInstance()
                if (!context.isFinishing) {
                    manager.beginTransaction().replace(R.id.content_frame, currentFragment!!).commitAllowingStateLoss()
                    sharedPref.edit().putString(context.getString(R.string.pref_lastFragment), name).apply()
                    lastId = request.id
                    val toolbar = context.supportActionBar
                    if (toolbar != null) {
                        toolbar.title = context.getString(request.id)
                    }
                    drawer.setSelection(request.id.toLong())
                }
            }
        }
    }

    fun loadLastFragment(): Boolean {
        val fragment = context.intent.getIntExtra(EXTRA_MODE, 0)
        if (fragment != 0) {
            EventBus.getDefault().post(SwitchFragmentRequest(fragment))
        } else if (sharedPref.contains(context.getString(R.string.pref_lastFragment))) {
            val load = context.resources.getIdentifier(sharedPref.getString(context.getString(R.string.pref_lastFragment), ""), "id", context.packageName)
            EventBus.getDefault().post(SwitchFragmentRequest(load))
            return true
        }
        return false
    }

    companion object {
        const val EXTRA_MODE = "mode"
    }
}
