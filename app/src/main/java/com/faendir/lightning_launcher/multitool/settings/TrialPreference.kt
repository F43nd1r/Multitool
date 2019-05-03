package com.faendir.lightning_launcher.multitool.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.preference.Preference
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.billing.BaseBillingManager
import com.faendir.lightning_launcher.multitool.billing.BillingManager

/**
 * @author F43nd1r
 * @since 12.11.2016
 */
@Suppress("unused")
class TrialPreference(c: Context, attrs: AttributeSet) : Preference(c, attrs) {
    private val billingManager: BillingManager?
    private var isBought = false
    private var trialState: BaseBillingManager.TrialState = BaseBillingManager.TrialState.NOT_STARTED
    private val feature: BaseBillingManager.PaidFeature

    init {
        var context = c
        isPersistent = false
        val a = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.Preference, androidx.preference.R.attr.preferenceStyle, 0)
        val value = a.getResourceId(androidx.preference.R.styleable.Preference_android_title, 0)
        val res = a.getResourceId(androidx.preference.R.styleable.Preference_title, value)
        feature = BaseBillingManager.PaidFeature.fromTitle(res)?: throw IllegalArgumentException()
        a.recycle()
        if (context is ContextWrapper) {
            context = context.baseContext
        }
        if (context is Activity) {
            billingManager = BillingManager(context)
            update()
        } else {
            billingManager = null
        }
    }

    private fun update() {
        Thread {
            val summary: String
            isBought = billingManager!!.isBought(feature)
            if (isBought) {
                summary = context.getString(R.string.summary_unlocked)
            } else {
                trialState = billingManager.isTrial(feature)
                summary = when (trialState) {
                    BaseBillingManager.TrialState.NOT_STARTED -> context.getString(R.string.summary_notStarted)
                    BaseBillingManager.TrialState.ONGOING -> context.getString(R.string.summary_ongoing, DateFormat.getDateFormat(context).format(billingManager.getExpiration(feature).time))
                    BaseBillingManager.TrialState.EXPIRED -> context.getString(R.string.summary_used)
                    BaseBillingManager.TrialState.UNKNOWN -> context.getString(R.string.summary_unknown)
                }
            }
            Handler(context.mainLooper).post { setSummary(summary) }
        }.start()
    }

    override fun onClick() {
        if (!isBought) {
            when (trialState) {
                BaseBillingManager.TrialState.NOT_STARTED -> billingManager?.showTrialDialog(feature, Runnable { this.update() })
                BaseBillingManager.TrialState.ONGOING, BaseBillingManager.TrialState.EXPIRED -> billingManager?.showBuyDialog(feature, Runnable { this.update() })
            }
        }
    }

    override fun onPrepareForRemoval() {
        super.onPrepareForRemoval()
        billingManager?.release()
    }
}
