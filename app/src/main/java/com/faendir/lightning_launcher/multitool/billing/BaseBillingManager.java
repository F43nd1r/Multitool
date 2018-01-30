package com.faendir.lightning_launcher.multitool.billing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.LambdaUtils.ExceptionalRunnable;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;
import static com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG;
import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.ignoreExceptions;

/**
 * @author F43nd1r
 * @since 07.10.2016
 */
public class BaseBillingManager implements BillingProcessor.IBillingHandler {
    public enum TrialState {
        NOT_STARTED,
        ONGOING,
        EXPIRED
    }

    private static final String MUSIC_WIDGET = "music_widget";
    private static final String DRAWER = "drawer";
    private static final String ANIMATION = "animation";
    static final int SEVEN_DAYS_IN_SECONDS = 60 * 60 * 24 * 7;
    private final Context context;
    private final BillingProcessor billingProcessor;
    final Map<String, Long> expiration;
    final BidiMap<Integer, String> mapping;
    private boolean error = false;

    public BaseBillingManager(Context context) {
        if (BillingProcessor.isIabServiceAvailable(context)) {
            billingProcessor = new BillingProcessor(context, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr" +
                    "oO1TQI/CyB/rVxPAe9sgzr253BpS95MQrYHkUSC3ntC1d9rXwoFT8XenCqFhrwsi6Kr5muUoNssNEkgBuvM" +
                    "DnY18JQlr8dHLltah3WyuBndSbAlHDnGKoac0YrqSPBzCLZ2LWc5Ok0GvEmz3fnKXGlha8/fzZdV3cUYtJXU" +
                    "jdRF42iE/QxANHuP3olT1SmfrC0fEaSpaaxeGIBf2l/nBK8YA4g4bQDa4A4uFJX4BHgRcvG5RNpSAW6MDhNt" +
                    "qy1221c566scH3otwsT7gK5d+peK4nmx4hJacYFJUuVHqjkEgcVW9AuNtigzb7aSmumZSSVH4N4cnH7dCz4g" +
                    "ffU1hwIDAQAB", this);

        } else {
            billingProcessor = null;
            error = true;
        }
        this.context = context;
        expiration = new HashMap<>();
        mapping = new DualHashBidiMap<>();
        mapping.put(R.string.title_musicWidget, MUSIC_WIDGET);
        mapping.put(R.string.title_drawer, DRAWER);
        mapping.put(R.string.title_animation, ANIMATION);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        this.error = true;
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public void onBillingInitialized() {
        billingProcessor.loadOwnedPurchasesFromGoogle();
        synchronized (this) {
            notifyAll();
        }
    }

    BillingProcessor getBillingProcessor() {
        return billingProcessor;
    }

    public void release() {
        if (billingProcessor != null) billingProcessor.release();
    }

    @WorkerThread
    public boolean isBoughtOrTrial(@StringRes int id) {
        final String name = mapping.get(id);
        return name == null || init() && billingProcessor.isPurchased(name) || isTrial(name) == TrialState.ONGOING;
    }

    @WorkerThread
    public boolean isBought(@StringRes int id) {
        String name = mapping.get(id);
        if (name == null) {
            return true;
        }
        if (init()) {
            boolean result = billingProcessor.isPurchased(name);
            if (DEBUG) Log.d(LOG_TAG, name + " isBought " + result);
            return result;
        }
        return false;
    }

    @WorkerThread
    public TrialState isTrial(@StringRes int id) {
        String name = mapping.get(id);
        TrialState result = name == null ? TrialState.NOT_STARTED : isTrial(name);
        if (DEBUG) Log.d(LOG_TAG, name + " isTrial " + result.name());
        return result;
    }

    @WorkerThread
    public Calendar getExpiration(@StringRes int id) {
        String name = mapping.get(id);
        Calendar calendar = Calendar.getInstance();
        long expiration;
        if (name != null && (expiration = getExpiration(name)) != -1) {
            calendar.setTimeInMillis(expiration * 1000);
        }
        return calendar;
    }

    private long getExpiration(String productId) {
        long expires;
        if (expiration.containsKey(productId)) {
            expires = expiration.get(productId);
        } else {
            int time = networkRequest(productId, 0);
            if (time == -1) {
                expires = -1;
            } else {
                expires = System.currentTimeMillis() / 1000 + SEVEN_DAYS_IN_SECONDS - time;
            }
            expiration.put(productId, expires);
        }
        return expires;
    }

    TrialState isTrial(String productId) {
        long expires = getExpiration(productId);
        TrialState result;
        if (expires == -1) {
            result = TrialState.NOT_STARTED;
        } else if (System.currentTimeMillis() / 1000 < expires) {
            result = TrialState.ONGOING;
        } else {
            result = TrialState.EXPIRED;
        }
        if (DEBUG) Log.d(LOG_TAG, productId + " TrialState " + result.name());
        return result;
    }

    @SuppressLint("HardwareIds")
    int networkRequest(String productId, int requestId) {
        try {
            String charset = "UTF-8";
            HttpURLConnection connection = (HttpURLConnection) new URL("https://faendir.com/android/index.php").openConnection();
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

    @CheckResult
    boolean init() {
        while (!error && !billingProcessor.isInitialized()) {
            synchronized (this) {
                ignoreExceptions((ExceptionalRunnable) this::wait).run();
            }
        }
        return !error;
    }
}
