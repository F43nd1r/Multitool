package com.faendir.lightning_launcher.multitool.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.preference.Preference;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.billing.BaseBillingManager;
import com.faendir.lightning_launcher.multitool.billing.BillingManager;

import java.util.Calendar;

/**
 * Created by Lukas on 12.11.2016.
 */

public class TrialPreference extends Preference {
    private final BillingManager billingManager;
    private boolean isBought = false;
    private BaseBillingManager.TrialState trialState = BaseBillingManager.TrialState.NOT_STARTED;

    public TrialPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        if (context instanceof Activity) {
            billingManager = new BillingManager((Activity) context);
            update();
        } else {
            billingManager = null;
        }
    }

    private void update() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String summary;
                int res = getTitleRes();
                isBought = billingManager.isBought(res);
                if (isBought) {
                    summary = getContext().getString(R.string.summary_unlocked);
                } else {
                    trialState = billingManager.isTrial(res);
                    switch (trialState) {
                        case NOT_STARTED:
                            summary = getContext().getString(R.string.summary_notStarted);
                            break;
                        case ONGOING:
                            summary = getContext().getString(R.string.summary_ongoing, DateFormat.getDateFormat(getContext()).format(billingManager.getExpiration(res).getTime()));
                            break;
                        case EXPIRED:
                            summary = getContext().getString(R.string.summary_used);
                            break;
                        default:
                            summary = getContext().getString(R.string.summary_unknown);
                    }
                }
                new Handler(getContext().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setSummary(summary);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onClick() {
        if (!isBought) {
            switch (trialState) {
                case NOT_STARTED:
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.title_trial)
                            .setMessage(R.string.message_trial)
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            billingManager.startTrial(getTitleRes());
                                            update();
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton(R.string.button_cancel, null)
                            .show();
                    break;
                case ONGOING:
                case EXPIRED:
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.title_buy)
                            .setMessage(getContext().getString(R.string.message_buy, getTitle()))
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            billingManager.buy(getTitleRes());
                                            update();
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton(R.string.button_cancel, null)
                            .show();
                    break;
            }

        }
    }

    @Override
    protected void onPrepareForRemoval() {
        billingManager.release();
    }
}
