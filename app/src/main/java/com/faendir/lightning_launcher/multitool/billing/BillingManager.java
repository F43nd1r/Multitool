package com.faendir.lightning_launcher.multitool.billing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import android.util.Log;
import android.widget.Toast;

import com.anjlab.android.iab.v3.TransactionDetails;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest;
import java9.util.function.IntConsumer;
import org.greenrobot.eventbus.EventBus;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;
import static com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG;

/**
 * @author F43nd1r
 * @since 29.09.2016
 */

public class BillingManager extends BaseBillingManager {
    private final Activity context;

    public BillingManager(Activity context) {
        super(context);
        this.context = context;
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return getBillingProcessor().handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        super.onProductPurchased(productId, details);
        EventBus.getDefault().post(new SwitchFragmentRequest(mapping.inverse().get(productId)));
    }

    @UiThread
    public void showTrialBuyDialog(@StringRes int which) {
        showTrialBuyDialog(which, null);
    }

    @UiThread
    public void showTrialBuyDialog(@StringRes final int which, @Nullable final Runnable onClose) {
        if (!context.isFinishing()) {
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.text_buyOrTrial, context.getString(which)))
                    .setPositiveButton(R.string.button_buy, (dialog1, ignore) -> {
                        if (DEBUG) Log.d(LOG_TAG, "Button buy");
                        buy(which);
                        runIfNotNull(onClose);
                    })
                    .setNeutralButton(R.string.button_trial, (dialog1, ignore) -> {
                        if (DEBUG) Log.d(LOG_TAG, "Button trial");
                        new Thread(() -> {
                            startTrial(which);
                            runIfNotNull(onClose);
                        }).start();
                    })
                    .setNegativeButton(R.string.button_cancel, (dialog1, ignore) -> {
                        if (DEBUG) Log.d(LOG_TAG, "Button cancel");
                        if (onClose != null) onClose.run();
                    })
                    .setOnCancelListener(dialogInterface -> runIfNotNull(onClose))
                    .setCancelable(false)
                    .show();
        }
    }

    public void showTrialDialog(@StringRes final int which, @Nullable final Runnable onClose) {
        showDialog(R.string.title_trial, R.string.message_trial, which, this::startTrial, onClose);
    }

    public void showBuyDialog(@StringRes final int which, @Nullable final Runnable onClose) {
        showDialog(R.string.title_buy, R.string.message_buy, which, this::buy, onClose);
    }

    private void showDialog(@StringRes int title, @StringRes int message, @StringRes final int which, @NonNull IntConsumer onPositive, @Nullable final Runnable onClose) {
        if (!context.isFinishing()) {
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(context.getString(message, context.getString(which)))
                    .setPositiveButton(R.string.button_ok, (dialog, ignore) -> new Thread(() -> {
                        buy(which);
                        runIfNotNull(onClose);
                    }).start())
                    .setNegativeButton(R.string.button_cancel, (dialogInterface, i) -> runIfNotNull(onClose))
                    .setOnCancelListener(dialogInterface -> runIfNotNull(onClose))
                    .setCancelable(false)
                    .show();
        }
    }

    private void runIfNotNull(Runnable runnable) {
        if (runnable != null) runnable.run();
    }

    private void buy(@StringRes int id) {
        if (init()) {
            getBillingProcessor().purchase(context, mapping.get(id));
        } else {
            context.runOnUiThread(() -> Toast.makeText(context, R.string.toast_playError, Toast.LENGTH_LONG).show());
        }
    }

    private void startTrial(@StringRes int id) {
        String productId = mapping.get(id);
        TrialState state = isTrial(productId);
        if (state == TrialState.EXPIRED) {
            context.runOnUiThread(() -> Toast.makeText(context, R.string.toast_trialUsed, Toast.LENGTH_LONG).show());
        } else {
            int result = networkRequest(productId, 1);
            if (result != 0) {
                context.runOnUiThread(() -> Toast.makeText(context, R.string.toast_error, Toast.LENGTH_LONG).show());
            } else {
                expiration.put(productId, System.currentTimeMillis() / 1000 + SEVEN_DAYS_IN_SECONDS);
                EventBus.getDefault().post(new SwitchFragmentRequest(mapping.inverse().get(productId)));
            }
        }
    }
}
