package com.faendir.lightning_launcher.multitool.settings;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import com.faendir.lightning_launcher.multitool.R;

/**
 * @author F43nd1r
 * @since 21.06.18
 */
public class IdPreference extends DialogPreference {
    @SuppressLint("HardwareIds")
    public IdPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogMessage(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
    }

    public static class Dialog extends PreferenceDialogFragmentCompat {
        private static final String KEY_MESSAGE = "IdPreference.message";
        private CharSequence message;

        public static Dialog newInstance(String key) {
            final Dialog fragment = new Dialog();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                message = getPreference().getDialogMessage();
            } else {
                message = savedInstanceState.getCharSequence(KEY_MESSAGE);
            }
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putCharSequence(KEY_MESSAGE, message);
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            if (positiveResult) {
                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, message));
                }
                Toast.makeText(getContext(), R.string.toast_clipboard, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
