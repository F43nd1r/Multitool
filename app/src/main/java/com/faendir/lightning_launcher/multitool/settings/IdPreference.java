package com.faendir.lightning_launcher.multitool.settings;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.Toast;

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

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, getDialogMessage()));
            }
            Toast.makeText(getContext(), R.string.toast_clipboard, Toast.LENGTH_SHORT).show();
        }
        dialog.dismiss();
    }
}
