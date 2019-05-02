package com.faendir.lightning_launcher.multitool.gesture

import android.app.Activity
import android.content.Intent
import android.gesture.Gesture
import android.gesture.GestureOverlayView
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.util.BaseActivity

class GestureActivity : BaseActivity(R.layout.content_gesture), GestureOverlayView.OnGesturePerformedListener, View.OnClickListener {

    private var info: GestureInfo? = null
    private var gesture: Gesture? = null
    private lateinit var chooseAction: Button
    private lateinit var label: EditText
    private var action: Intent? = null
    private lateinit var gestureView: GestureOverlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        gestureView = findViewById(R.id.gesture_view)
        gestureView.isFadeEnabled = true
        gestureView.fadeOffset = java.lang.Long.MAX_VALUE
        gestureView.addOnGesturePerformedListener(this)
        chooseAction = findViewById(R.id.button_choose_action)
        chooseAction.setOnClickListener(this)
        findViewById<View>(R.id.button_confirm).setOnClickListener(this)
        label = findViewById(R.id.editText_name)
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(GESTURE)) {
            val gestureInfo: GestureInfo = intent.getParcelableExtra(GESTURE)
            info = gestureInfo
            val gesture = gestureInfo.getGesture(this)
            if (gesture != null) {
                gestureView.post { gestureView.gesture = gesture }
            }
            label.setText(gestureInfo.name)
            action = gestureInfo.intent
            val pm = packageManager
            val label = pm.resolveActivity(gestureInfo.intent, 0).activityInfo.loadLabel(pm).toString()
            chooseAction.text = label
        }
    }

    override fun onGesturePerformed(overlay: GestureOverlayView, gesture: Gesture) {
        this.gesture = gesture
        overlay.cancelClearAnimation()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_choose_action -> startActivityForResult(Intent(this, IntentChooser::class.java), 0)
            R.id.button_confirm -> confirm()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            action = data.getParcelableExtra(Intent.EXTRA_INTENT)
            val label = data.getStringExtra(Intent.EXTRA_TITLE)
            chooseAction.text = label
            if (this.label.text.isEmpty()) {
                this.label.setText(label)
            }
        }
    }

    private fun confirm() {
        gesture = gestureView.gesture
        val action = action
        val gesture = gesture
        when {
            action == null -> Toast.makeText(this, R.string.toast_noAction, Toast.LENGTH_SHORT).show()
            gesture == null -> Toast.makeText(this, R.string.toast_noGesture, Toast.LENGTH_SHORT).show()
            label.text.isEmpty() -> Toast.makeText(this, R.string.toast_noLabel, Toast.LENGTH_SHORT).show()
            else -> {
                val data = intent
                val gestureInfo = info?.also {
                    it.intent = action
                    it.name = label.text.toString()
                } ?: GestureInfo(action, label.text.toString())
                gestureInfo.setGesture(this, gesture)
                data.putExtra(GESTURE, gestureInfo)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }

    companion object {
        const val GESTURE = "gesture"
    }
}
