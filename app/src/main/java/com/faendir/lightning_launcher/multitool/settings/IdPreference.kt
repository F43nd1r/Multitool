package com.faendir.lightning_launcher.multitool.settings

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import com.faendir.lightning_launcher.multitool.R

/**
 * @author F43nd1r
 * @since 21.06.18
 */
class IdPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    init {
        @SuppressLint("HardwareIds")
        dialogMessage = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    class Dialog : PreferenceDialogFragmentCompat() {
        private lateinit var message: CharSequence

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            message = savedInstanceState?.getCharSequence(KEY_MESSAGE) ?: preference.dialogMessage
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putCharSequence(KEY_MESSAGE, message)
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            if (positiveResult) {
                val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                if (clipboardManager != null) {
                    clipboardManager.primaryClip = ClipData.newPlainText(null, message)
                }
                Toast.makeText(context, R.string.toast_clipboard, Toast.LENGTH_SHORT).show()
            }
        }

        companion object {
            private const val KEY_MESSAGE = "IdPreference.message"

            fun newInstance(key: String): Dialog {
                val fragment = Dialog()
                val b = Bundle(1)
                b.putString(ARG_KEY, key)
                fragment.arguments = b
                return fragment
            }
        }
    }
}
