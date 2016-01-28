package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Build;
import android.view.MotionEvent;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.FileManagerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lukas on 26.01.2016.
 */
public class LightningGestureView extends GestureOverlayView implements GestureOverlayView.OnGesturePerformedListener {
    private FileManager<GestureInfo> fileManager;
    private android.gesture.GestureLibrary library;
    private List<GestureInfo> gestureInfos;

    public LightningGestureView(Context context) {
        super(context);
        Toast.makeText(getContext(), "Created", Toast.LENGTH_SHORT).show();
        addOnGesturePerformedListener(this);
        int color = context.getResources().getColor(R.color.accent);
        setGestureColor(color);
        setUncertainGestureColor(color);
        setEventsInterceptionEnabled(true);
        fileManager = FileManagerFactory.createGestureFileManager(context);
        gestureInfos = fileManager.read();
        library = SingletonGestureLibrary.getGlobal(context);
        for (int i = 0; i < gestureInfos.size(); i++) {
            library.addGesture(String.valueOf(i), gestureInfos.get(i).getGesture(context));
        }
        post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    setOnGenericMotionListener(null);
                }
                setOnTouchListener(null);
                setOnClickListener(null);
                setOnDragListener(null);
                setOnLongClickListener(null);
            }
        });
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        Toast.makeText(getContext(), "Got gesture", Toast.LENGTH_SHORT).show();
        ArrayList<Prediction> list = library.recognize(gesture);
        if (list.size() != 0 && list.get(0).score >= 1.0) {
            try {
                Intent intent = gestureInfos.get(Integer.parseInt(list.get(0).name)).getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Something went wrong while recognizing a gesture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
