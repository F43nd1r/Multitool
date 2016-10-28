package com.faendir.lightning_launcher.multitool.billing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.UiThread;
import android.view.WindowManager;
import android.widget.Toast;

import com.anjlab.android.iab.v3.TransactionDetails;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest;

import org.greenrobot.eventbus.EventBus;

/**
 * @author F43nd1r
 * @since 29.09.2016
 */

public class BillingManager extends BaseBillingManager {
    private final Activity context;
    private volatile boolean ready = false;

    public BillingManager(Activity context) {
        super(context);
        this.context = context;
        getBillingProcessor().loadOwnedPurchasesFromGoogle();
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return getBillingProcessor().handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBillingInitialized() {
        synchronized (this) {
            ready = true;
            notifyAll();
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        super.onProductPurchased(productId, details);
        EventBus.getDefault().post(new SwitchFragmentRequest(R.string.title_musicWidget));
    }

    @UiThread
    public void showDialog() {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage("Music widget is a paid feature. You can either start a 7-day Trial or buy it.")
                .setPositiveButton("Buy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        buy(MUSIC_WIDGET);
                    }
                })
                .setNeutralButton("Trial", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                startTrial(MUSIC_WIDGET);
                            }
                        }).start();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .create();
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.show();
    }

    private void buy(String productId) {
        synchronized (this) {
            while (!ready) {
                try {
                    wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        getBillingProcessor().purchase(context, productId);
    }

    private void startTrial(String productId) {
        TrialState state = isTrial(productId);
        if (state == TrialState.EXPIRED) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "You've already used your Trial period.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            int result = networkRequest(productId, 1);
            if (result != 0) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Sorry, something went wrong.", Toast.LENGTH_LONG).show();
                    }
                });
            }else {
                EventBus.getDefault().post(new SwitchFragmentRequest(R.string.title_musicWidget));
            }
        }

    }

}
