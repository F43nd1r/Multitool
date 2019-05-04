package com.faendir.lightning_launcher.multitool.gesture

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.gesture.Gesture
import android.gesture.GestureOverlayView
import android.gesture.Prediction
import android.util.Log
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.faendir.lightning_launcher.multitool.BuildConfig
import com.faendir.lightning_launcher.multitool.MultiTool.Companion.DEBUG
import com.faendir.lightning_launcher.multitool.MultiTool.Companion.LOG_TAG
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.Utils
import java9.util.stream.StreamSupport
import java.util.*

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
@Keep
class LightningGestureView private constructor(lightningContext: Context, packageContext: Context) : GestureOverlayView(lightningContext), GestureOverlayView.OnGesturePerformedListener {
    init {
        addOnGesturePerformedListener(this)
        val color = ContextCompat.getColor(packageContext, R.color.accent)
        gestureColor = color
        uncertainGestureColor = color
        isEventsInterceptionEnabled = true
        if (DEBUG) Log.d(LOG_TAG, "Created gesture view")
    }

    @Throws(PackageManager.NameNotFoundException::class)
    constructor(context: Context) : this(context, context.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY))

    constructor(utils: Utils) : this(utils.lightningContext, utils.multitoolContext)

    override fun onGesturePerformed(overlay: GestureOverlayView, gesture: Gesture) {
        if (DEBUG) Log.d(LOG_TAG, "Gesture performed")
        val gestureInfos = GestureUtils.readFromFile(context)
        if (DEBUG) Log.d(LOG_TAG, "GestureInfos loaded")
        var recognized = false
        if (!gestureInfos.isEmpty()) {
            val library = SingleStoreGestureLibrary.getInstance(context)
            if (DEBUG) Log.d(LOG_TAG, "Gestures loaded")
            val list: List<Prediction>? = library.recognize(gesture)
            if (list != null && list.isNotEmpty()) {
                if (DEBUG) Log.d(LOG_TAG, "Gesture recognized")
                try {
                    val uuid = UUID.fromString(list[0].name)
                    if (DEBUG) Log.d(LOG_TAG, "Gesture UUID $uuid")
                    val info = StreamSupport.stream(gestureInfos).filter { gestureInfo -> gestureInfo.hasUuid(uuid) }.findAny()
                    if (info.isPresent) {
                        recognized = true
                        val intent = info.get().intent
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        if (DEBUG) Log.d(LOG_TAG, "Gesture launched")
                    } else if (DEBUG) Log.w(LOG_TAG, "Bad gesture UUID")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Something went wrong while recognizing a gesture", Toast.LENGTH_SHORT).show()
                }

            }
        }
        if (!recognized) {
            if (DEBUG) Log.d(LOG_TAG, "Gesture not recognized")
            Toast.makeText(context, "Gesture not recognized", Toast.LENGTH_SHORT).show()
        }
    }
}
