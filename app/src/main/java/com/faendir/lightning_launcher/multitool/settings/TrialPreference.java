package com.faendir.lightning_launcher.multitool.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.preference.Preference;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.billing.BaseBillingManager;
import com.faendir.lightning_launcher.multitool.billing.BillingManager;

/**
 * @author F43nd1r
 * @since 12.11.2016
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
        new Thread(() -> {
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
            new Handler(getContext().getMainLooper()).post(() -> setSummary(summary));
        }).start();
    }

    @Override
    protected void onClick() {
        if (!isBought) {
            switch (trialState) {
                case NOT_STARTED:
                    billingManager.showTrialDialog(getTitleRes(), this::update);
                    break;
                case ONGOING:
                case EXPIRED:
                    billingManager.showBuyDialog(getTitleRes(), this::update);
                    break;
            }

        }
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        billingManager.release();
    }
}
