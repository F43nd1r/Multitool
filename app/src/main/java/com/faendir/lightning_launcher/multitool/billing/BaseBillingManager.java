package com.faendir.lightning_launcher.multitool.billing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.WorkerThread;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.faendir.lightning_launcher.multitool.util.Fragments;
import com.faendir.lightning_launcher.multitool.util.LambdaUtils.ExceptionalRunnable;
import java9.util.Optional;
import java9.util.stream.Stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

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

    public enum PaidFeature {
        MUSIC_WIDGET("music_widget", Fragments.MUSIC),
        DRAWER("drawer", Fragments.DRAWER),
        ANIMATION("animation", Fragments.ANIMATION);
        private final String id;
        private final Fragments relatedFragment;
        private Long expiration;

        PaidFeature(String id, Fragments relatedFragment) {
            this.id = id;
            this.relatedFragment = relatedFragment;
        }

        public String getId() {
            return id;
        }

        public Fragments getRelatedFragment() {
            return relatedFragment;
        }

        public static Optional<PaidFeature> fromTitle(@StringRes int titleRes) {
            return Stream.of(PaidFeature.values()).filter(v -> v.relatedFragment.getRes() == titleRes).findAny();
        }

        public static Optional<PaidFeature> fromId(@NonNull String productId) {
            return Stream.of(PaidFeature.values()).filter(v -> v.id.equals(productId)).findAny();
        }

        public static Optional<PaidFeature> fromFragment(@NonNull Fragments fragment) {
            return Stream.of(PaidFeature.values()).filter(v -> v.relatedFragment == fragment).findAny();
        }
    }

    private static final int SEVEN_DAYS_IN_SECONDS = 60 * 60 * 24 * 7;
    private final Context context;
    private final BillingProcessor billingProcessor;
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
        if(!error && billingProcessor != null) {
            billingProcessor.loadOwnedPurchasesFromGoogle();
            synchronized (this) {
                notifyAll();
            }
        }
    }

    BillingProcessor getBillingProcessor() {
        return billingProcessor;
    }

    public void release() {
        if (billingProcessor != null) billingProcessor.release();
    }

    @WorkerThread
    public boolean isBoughtOrTrial(@NonNull PaidFeature feature) {
        return init() && billingProcessor.isPurchased(feature.id) || isTrial(feature) == TrialState.ONGOING;
    }

    @WorkerThread
    public boolean isBought(@NonNull PaidFeature feature) {
        if (init()) {
            boolean result = billingProcessor.isPurchased(feature.id);
            if (DEBUG) Log.d(LOG_TAG, feature + " isBought " + result);
            return result;
        }
        return false;
    }

    @WorkerThread
    public TrialState isTrial(@NonNull PaidFeature feature) {
        TrialState result = isTrialImpl(feature);
        if (DEBUG) Log.d(LOG_TAG, feature + " isTrial " + result);
        return result;
    }

    @WorkerThread
    public Calendar getExpiration(@NonNull PaidFeature feature) {
        Calendar calendar = Calendar.getInstance();
        long expiration;
        if ((expiration = getExpirationImpl(feature)) != -1) {
            calendar.setTimeInMillis(expiration * 1000);
        }
        return calendar;
    }

    private long getExpirationImpl(@NonNull PaidFeature feature) {
        long expires;
        if (feature.expiration != null) {
            expires = feature.expiration;
        } else {
            int time = networkRequest(feature.id, 0);
            if (time == -1) {
                expires = -1;
            } else {
                expires = System.currentTimeMillis() / 1000 + SEVEN_DAYS_IN_SECONDS - time;
            }
            feature.expiration = expires;
        }
        return expires;
    }

    TrialState isTrialImpl(@NonNull PaidFeature feature) {
        long expires = getExpirationImpl(feature);
        TrialState result;
        if (expires == -1) {
            result = TrialState.NOT_STARTED;
        } else if (System.currentTimeMillis() / 1000 < expires) {
            result = TrialState.ONGOING;
        } else {
            result = TrialState.EXPIRED;
        }
        if (DEBUG) Log.d(LOG_TAG, feature + " TrialState " + result);
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

    @WorkerThread
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
