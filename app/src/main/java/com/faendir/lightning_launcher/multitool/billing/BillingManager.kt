package com.faendir.lightning_launcher.multitool.billing

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import com.anjlab.android.iab.v3.TransactionDetails
import com.faendir.lightning_launcher.multitool.MultiTool.DEBUG
import com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest
import java9.util.function.Consumer
import org.greenrobot.eventbus.EventBus

/**
 * @author F43nd1r
 * @since 29.09.2016
 */

class BillingManager(private val context: Activity) : BaseBillingManager(context) {

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
        return billingProcessor.get()?.handleActivityResult(requestCode, resultCode, data) ?: false
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        super.onProductPurchased(productId, details)
        EventBus.getDefault().post(SwitchFragmentRequest(PaidFeature.fromId(productId)?.relatedFragment))
    }

    @UiThread
    @JvmOverloads
    fun showTrialBuyDialog(feature: PaidFeature, onClose: Runnable? = null) {
        if (!context.isFinishing) {
            AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.text_buyOrTrial, context.getString(feature.relatedFragment.res)))
                    .setPositiveButton(R.string.button_buy) { _, _ ->
                        if (DEBUG) Log.d(LOG_TAG, "Button buy")
                        buy(feature)
                        onClose?.run()
                    }
                    .setNeutralButton(R.string.button_trial) { _, _ ->
                        if (DEBUG) Log.d(LOG_TAG, "Button trial")
                        Thread {
                            startTrial(feature)
                            onClose?.run()
                        }.start()
                    }
                    .setNegativeButton(R.string.button_cancel) { _, _ ->
                        if (DEBUG) Log.d(LOG_TAG, "Button cancel")
                        onClose?.run()
                    }
                    .setOnCancelListener {
                        onClose?.run()
                    }
                    .setCancelable(false)
                    .show()
        }
    }

    fun showTrialDialog(feature: PaidFeature, onClose: Runnable?) {
        showDialog(R.string.title_trial, R.string.message_trial, feature, Consumer { this.startTrial(it) }, onClose)
    }

    fun showBuyDialog(feature: PaidFeature, onClose: Runnable?) {
        showDialog(R.string.title_buy, R.string.message_buy, feature, Consumer { this.buy(it) }, onClose)
    }

    private fun showDialog(@StringRes title: Int, @StringRes message: Int, feature: PaidFeature, onPositive: Consumer<PaidFeature>, onClose: Runnable?) {
        if (!context.isFinishing) {
            AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(context.getString(message, context.getString(feature.relatedFragment.res)))
                    .setPositiveButton(R.string.button_ok) { _, _ ->
                        Thread {
                            onPositive.accept(feature)
                            onClose?.run()
                        }.start()
                    }
                    .setNegativeButton(R.string.button_cancel) { _, _ ->
                        onClose?.run()
                    }
                    .setOnCancelListener {
                        onClose?.run()
                    }
                    .setCancelable(false)
                    .show()
        }
    }

    private fun buy(feature: PaidFeature) {
        val p = billingProcessor.get()
        if (p != null) {
            p.purchase(context, feature.id)
        } else {
            context.runOnUiThread { Toast.makeText(context, R.string.toast_playError, Toast.LENGTH_LONG).show() }
        }
    }

    private fun startTrial(feature: PaidFeature) {
        val state = isTrial(feature)
        if (state === TrialState.EXPIRED) {
            context.runOnUiThread { Toast.makeText(context, R.string.toast_trialUsed, Toast.LENGTH_LONG).show() }
        } else {
            val result = networkRequest(feature.id, 1)
            if (result != 0) {
                context.runOnUiThread { Toast.makeText(context, R.string.toast_error, Toast.LENGTH_LONG).show() }
            } else {
                EventBus.getDefault().post(SwitchFragmentRequest(feature.relatedFragment))
            }
        }
    }
}
