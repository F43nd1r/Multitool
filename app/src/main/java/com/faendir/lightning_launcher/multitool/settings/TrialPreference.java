package com.faendir.lightning_launcher.multitool.settings;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import androidx.preference.Preference;
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
    private final BaseBillingManager.PaidFeature feature;

    public TrialPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        final TypedArray a = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.Preference, androidx.preference.R.attr.preferenceStyle, 0);
        int val = a.getResourceId(androidx.preference.R.styleable.Preference_android_title, 0);
        int res = a.getResourceId(androidx.preference.R.styleable.Preference_title, val);
        feature = BaseBillingManager.PaidFeature.fromTitle(res).orElseThrow();
        a.recycle();
        if (context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
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
            isBought = billingManager.isBought(feature);
            if (isBought) {
                summary = getContext().getString(R.string.summary_unlocked);
            } else {
                trialState = billingManager.isTrial(feature);
                switch (trialState) {
                    case NOT_STARTED:
                        summary = getContext().getString(R.string.summary_notStarted);
                        break;
                    case ONGOING:
                        summary = getContext().getString(R.string.summary_ongoing, DateFormat.getDateFormat(getContext()).format(billingManager.getExpiration(feature).getTime()));
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
                    billingManager.showTrialDialog(feature, this::update);
                    break;
                case ONGOING:
                case EXPIRED:
                    billingManager.showBuyDialog(feature, this::update);
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
