package com.faendir.lightning_launcher.multitool.billing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @author F43nd1r
 * @since 07.10.2016
 */
public class BaseBillingManager implements BillingProcessor.IBillingHandler{
    static final String MUSIC_WIDGET = "music_widget";
    static final int SEVEN_DAYS_IN_SECONDS = 60 * 60 * 24 * 7;
    private final Context context;
    private final BillingProcessor billingProcessor;

    public BaseBillingManager(Context context) {
        billingProcessor = new BillingProcessor(context, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr" +
                "oO1TQI/CyB/rVxPAe9sgzr253BpS95MQrYHkUSC3ntC1d9rXwoFT8XenCqFhrwsi6Kr5muUoNssNEkgBuvM" +
                "DnY18JQlr8dHLltah3WyuBndSbAlHDnGKoac0YrqSPBzCLZ2LWc5Ok0GvEmz3fnKXGlha8/fzZdV3cUYtJXU" +
                "jdRF42iE/QxANHuP3olT1SmfrC0fEaSpaaxeGIBf2l/nBK8YA4g4bQDa4A4uFJX4BHgRcvG5RNpSAW6MDhNt" +
                "qy1221c566scH3otwsT7gK5d+peK4nmx4hJacYFJUuVHqjkEgcVW9AuNtigzb7aSmumZSSVH4N4cnH7dCz4g" +
                "ffU1hwIDAQAB", this);
        this.context = context;
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        reset();
    }

    @Override
    public void onBillingInitialized() {
    }

    BillingProcessor getBillingProcessor() {
        return billingProcessor;
    }

    public static boolean isIabServiceAvailable(Context context) {
        return BillingProcessor.isIabServiceAvailable(context);
    }

    public void release() {
        billingProcessor.release();
    }

    public boolean isBoughtOrTrial(long id) {
        if (id == R.string.title_musicWidget) {
            return billingProcessor.isPurchased(MUSIC_WIDGET) || isTrial(MUSIC_WIDGET);
        } else {
            return true;
        }
    }

    private boolean isTrial(String productId) {
        int time = networkRequest(productId, 0);
        return time != -1 && time < SEVEN_DAYS_IN_SECONDS;
    }

    @SuppressLint("HardwareIds")
    int networkRequest(String productId, int requestId) {
        try {
            String charset = "UTF-8";
            HttpURLConnection connection = (HttpURLConnection) new URL("http://faendir.com/android/index.php").openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
            connection.connect();
            connection.getOutputStream().write(String.format("user=%s&product=%s&request=%s",
                    URLEncoder.encode(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID), charset),
                    URLEncoder.encode(productId, charset),
                    URLEncoder.encode(String.valueOf(requestId), charset)).getBytes());
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = reader.readLine();
            return Integer.valueOf(result);
        } catch (IOException | NumberFormatException e) {
            return -1;
        }
    }

    void reset() {
        EventBus.getDefault().post(new SwitchFragmentRequest(0));
    }
}
