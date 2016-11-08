package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.FileManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
@SuppressWarnings("unused")
public class LightningGestureView extends GestureOverlayView implements GestureOverlayView.OnGesturePerformedListener {

    public LightningGestureView(Context context) {
        super(context);
        addOnGesturePerformedListener(this);
        int color = context.getResources().getColor(R.color.accent);
        setGestureColor(color);
        setUncertainGestureColor(color);
        setEventsInterceptionEnabled(true);
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        FileManager<GestureInfo> fileManager = FileManagerFactory.createGestureFileManager(getContext());
        List<GestureInfo> gestureInfos = fileManager.read();
        boolean recognized = false;
        if (gestureInfos != null) {
            GestureLibrary library = SingletonGestureLibrary.getGlobal(getContext());
            ArrayList<Prediction> list = library.recognize(gesture);
            if (list != null && list.size() != 0) {
                try {
                    UUID uuid = UUID.fromString(list.get(0).name);
                    for (GestureInfo info : gestureInfos) {
                        if (info.hasUuid(uuid)) {
                            recognized = true;
                            Intent intent = info.getIntent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(intent);
                            break;
                        }
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
