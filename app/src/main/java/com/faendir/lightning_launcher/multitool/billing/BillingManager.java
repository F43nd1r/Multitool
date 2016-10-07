package com.faendir.lightning_launcher.multitool.billing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.event.PurchaseRequest;

import org.greenrobot.eventbus.Subscribe;

/**
 * @author F43nd1r
 * @since 29.09.2016
 */

public class BillingManager extends BaseBillingManager  {
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

    public void showDialog() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                reset();
                            }
                        })
                        .setCancelable(false)
                        .create();
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.show();
            }
        });
    }

    @Subscribe
    public void onPurchaseRequest(PurchaseRequest purchaseRequest) {
        buy(purchaseRequest.getProductId());
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
        int time = networkRequest(productId, 0);
        if (time >= SEVEN_DAYS_IN_SECONDS) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "You've already used your Trial period.", Toast.LENGTH_LONG).show();
                    reset();
                }
            });
        } else {
            int result = networkRequest(productId, 1);
            if (result != 0) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Sorry, something went wrong.", Toast.LENGTH_LONG).show();
                        reset();
                    }
                });
            }
        }

    }

}
