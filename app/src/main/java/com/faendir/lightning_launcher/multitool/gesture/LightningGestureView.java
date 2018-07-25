package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Keep;
import androidx.core.content.ContextCompat;
import com.faendir.lightning_launcher.multitool.BuildConfig;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import java9.util.Optional;
import java9.util.stream.StreamSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;
import static com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG;

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
@Keep
public class LightningGestureView extends GestureOverlayView implements GestureOverlayView.OnGesturePerformedListener {
    private LightningGestureView(Context lightningContext, Context packageContext) {
        super(lightningContext);
        addOnGesturePerformedListener(this);
        int color = ContextCompat.getColor(packageContext, R.color.accent);
        setGestureColor(color);
        setUncertainGestureColor(color);
        setEventsInterceptionEnabled(true);
        if (DEBUG) Log.d(LOG_TAG, "Created gesture view");
    }

    public LightningGestureView(Context context) throws PackageManager.NameNotFoundException {
        this(context, context.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY));
    }

    public LightningGestureView(Utils utils) {
        this(utils.getLightningContext(), utils.getMultitoolContext());
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        if (DEBUG) Log.d(LOG_TAG, "Gesture performed");
        List<GestureInfo> gestureInfos = GestureUtils.readFromFile(getContext());
        if (DEBUG) Log.d(LOG_TAG, "GestureInfos loaded");
        boolean recognized = false;
        if (!gestureInfos.isEmpty()) {
            SingleStoreGestureLibrary library = SingleStoreGestureLibrary.getInstance(getContext());
            if (DEBUG) Log.d(LOG_TAG, "Gestures loaded");
            ArrayList<Prediction> list = library.recognize(gesture);
            if (list != null && list.size() != 0) {
                if (DEBUG) Log.d(LOG_TAG, "Gesture recognized");
                try {
                    UUID uuid = UUID.fromString(list.get(0).name);
                    Optional<GestureInfo> info = StreamSupport.stream(gestureInfos).filter(gestureInfo -> gestureInfo.hasUuid(uuid)).findAny();
                    if (info.isPresent()) {
                        recognized = true;
                        Intent intent = info.get().getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(intent);
                        if (DEBUG) Log.d(LOG_TAG, "Gesture launched");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Something went wrong while recognizing a gesture", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (!recognized) {
            Toast.makeText(getContext(), "Gesture not recognized", Toast.LENGTH_SHORT).show();
        }
    }
}
