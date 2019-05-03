package com.faendir.lightning_launcher.multitool.billing

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.faendir.lightning_launcher.multitool.MultiTool.DEBUG
import com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG
import com.faendir.lightning_launcher.multitool.util.Fragments
import com.google.common.util.concurrent.SettableFuture
import java9.util.Optional
import java9.util.stream.Stream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

/**
 * @author F43nd1r
 * @since 07.10.2016
 */
open class BaseBillingManager(private val context: Context) {
    internal val billingProcessor: SettableFuture<BillingProcessor?> = SettableFuture.create()

    enum class TrialState {
        NOT_STARTED,
        ONGOING,
        EXPIRED,
        UNKNOWN
    }

    enum class PaidFeature(val id: String, val relatedFragment: Fragments) {
        MUSIC_WIDGET("music_widget", Fragments.MUSIC),
        DRAWER("drawer", Fragments.DRAWER),
        ANIMATION("animation", Fragments.ANIMATION);

        internal var expiration: Long? = null

        companion object {

            @JvmStatic
            fun fromTitle(@StringRes titleRes: Int): PaidFeature? {
                return values().find { v -> v.relatedFragment.res == titleRes }
            }

            @JvmStatic
            fun fromId(productId: String): PaidFeature? {
                return values().find { v -> v.id == productId }
            }

            @JvmStatic
            fun fromFragment(fragment: Fragments): Optional<PaidFeature> {
                return Stream.of(*values()).filter { v -> v.relatedFragment === fragment }.findAny()
            }
        }
    }

    init {
        if (BillingProcessor.isIabServiceAvailable(context)) {
            object : BillingProcessor.IBillingHandler {
                val p = BillingProcessor(context, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr" +
                        "oO1TQI/CyB/rVxPAe9sgzr253BpS95MQrYHkUSC3ntC1d9rXwoFT8XenCqFhrwsi6Kr5muUoNssNEkgBuvM" +
                        "DnY18JQlr8dHLltah3WyuBndSbAlHDnGKoac0YrqSPBzCLZ2LWc5Ok0GvEmz3fnKXGlha8/fzZdV3cUYtJXU" +
                        "jdRF42iE/QxANHuP3olT1SmfrC0fEaSpaaxeGIBf2l/nBK8YA4g4bQDa4A4uFJX4BHgRcvG5RNpSAW6MDhNt" +
                        "qy1221c566scH3otwsT7gK5d+peK4nmx4hJacYFJUuVHqjkEgcVW9AuNtigzb7aSmumZSSVH4N4cnH7dCz4g" +
                        "ffU1hwIDAQAB", this)

                override fun onBillingInitialized() {
                    billingProcessor.set(p)
                }

                override fun onPurchaseHistoryRestored() {
                }

                override fun onProductPurchased(productId: String, details: TransactionDetails?) {
                    this@BaseBillingManager.onProductPurchased(productId, details)
                }

                override fun onBillingError(errorCode: Int, error: Throwable?) {
                    billingProcessor.set(null)
                }

            }

        } else {
            billingProcessor.set(null)
        }
    }

    open fun onProductPurchased(productId: String, details: TransactionDetails?) {
    }

    fun release() {
        billingProcessor.get()?.release()
    }

    @WorkerThread
    fun isBoughtOrTrial(feature: PaidFeature): Boolean {
        return billingProcessor.get()?.isPurchased(feature.id) ?: false || isTrial(feature) == TrialState.ONGOING
    }

    @WorkerThread
    fun isBought(feature: PaidFeature): Boolean {
        val result = billingProcessor.get()?.isPurchased(feature.id)
        if (DEBUG) Log.d(LOG_TAG, "$feature isBought $result")
        return result ?: false
    }

    @WorkerThread
    fun isTrial(feature: PaidFeature): TrialState {
        val result = isTrialImpl(feature)
        if (DEBUG) Log.d(LOG_TAG, "$feature isTrial $result")
        return result
    }

    @WorkerThread
    fun getExpiration(feature: PaidFeature): Calendar {
        val calendar = Calendar.getInstance()
        val expiration = getExpirationImpl(feature)
        if (expiration != null && expiration != -1L) {
            calendar.timeInMillis = expiration * 1000
        }
        return calendar
    }

    private val SEVEN_DAYS_IN_SECONDS = 7 * 24 * 60 * 60

    private fun getExpirationImpl(feature: PaidFeature): Long? {
        val expires: Long?
        if (feature.expiration != null) {
            expires = feature.expiration!!
        } else {
            val time = networkRequest(feature.id, 0)
            if (time != null) {
                expires = if (time == -1) -1 else System.currentTimeMillis() / 1000 + SEVEN_DAYS_IN_SECONDS - time
                feature.expiration = expires
            } else {
                expires = null
            }
        }
        return expires
    }

    private fun isTrialImpl(feature: PaidFeature): TrialState {
        val expires = getExpirationImpl(feature)
        val result: TrialState
        result = when {
            expires == null -> TrialState.UNKNOWN
            expires == -1L -> TrialState.NOT_STARTED
            System.currentTimeMillis() / 1000 < expires -> TrialState.ONGOING
            else -> TrialState.EXPIRED
        }
        if (DEBUG) Log.d(LOG_TAG, "$feature TrialState $result")
        return result
    }

    @SuppressLint("HardwareIds")
    internal fun networkRequest(productId: String, requestId: Int): Int? {
        try {
            val charset = "UTF-8"
            val connection = URL("https://faendir.com/android/index.php").openConnection() as HttpURLConnection
            connection.doInput = true
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.requestMethod = "POST"
            connection.connect()
            connection.outputStream.write(String.format("user=%s&product=%s&request=%s",
                    URLEncoder.encode(Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID), charset),
                    URLEncoder.encode(productId, charset),
                    URLEncoder.encode(requestId.toString(), charset)).toByteArray())
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val result = reader.readLine()
            if (DEBUG) Log.d(LOG_TAG, "Query $requestId result: $result")
            return Integer.valueOf(result)
        } catch (e: IOException) {
            return null
        } catch (e: NumberFormatException) {
            return null
        }

    }
}
