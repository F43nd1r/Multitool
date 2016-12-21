package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.BaseActivity;
import com.faendir.lightning_launcher.multitool.util.IntentChooser;

public class GestureActivity extends BaseActivity implements GestureOverlayView.OnGesturePerformedListener, View.OnClickListener {

    private GestureInfo info;
    private Gesture gesture;
    private Button chooseAction;
    private EditText label;
    private Intent action;
    private GestureOverlayView gestureView;

    public static final String GESTURE = "gesture";

    public GestureActivity() {
        super(R.layout.content_gesture);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        gestureView = (GestureOverlayView) findViewById(R.id.gesture_view);
        gestureView.setFadeEnabled(true);
        gestureView.setFadeOffset(Long.MAX_VALUE);
        gestureView.addOnGesturePerformedListener(this);
        chooseAction = (Button) findViewById(R.id.button_choose_action);
        chooseAction.setOnClickListener(this);
        findViewById(R.id.button_confirm).setOnClickListener(this);
        label = (EditText) findViewById(R.id.editText_name);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(GESTURE)) {
            info = intent.getParcelableExtra(GESTURE);
            Gesture gesture = info.getGesture(this);
            if(gesture != null) {
                gestureView.post(() -> gestureView.setGesture(gesture));
            }
            label.setText(info.getLabel());
            action = info.getIntent();
            PackageManager pm = getPackageManager();
            String label = pm.resolveActivity(info.getIntent(), 0).activityInfo.loadLabel(pm).toString();
            chooseAction.setText(label);
        }
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        this.gesture = gesture;
        overlay.cancelClearAnimation();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_choose_action:
                startActivityForResult(new Intent(this, IntentChooser.class), 0);
                break;
            case R.id.button_confirm:
                confirm();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            action = data.getParcelableExtra(Intent.EXTRA_INTENT);
            String label = data.getStringExtra(Intent.EXTRA_TITLE);
            chooseAction.setText(label);
            if (this.label.getText().length() == 0) {
                this.label.setText(label);
            }
        }
    }

    private void confirm() {
        gesture = gestureView.getGesture();
        if (action == null && info == null) {
            Toast.makeText(this, R.string.toast_noAction, Toast.LENGTH_SHORT).show();
        } else if (gesture == null && info == null) {
            Toast.makeText(this, R.string.toast_noGesture, Toast.LENGTH_SHORT).show();
        } else if (label.getText().length() == 0 && info == null) {
            Toast.makeText(this, R.string.toast_noLabel, Toast.LENGTH_SHORT).show();
        } else {
            Intent data = getIntent();
            if (info == null) {
                info = new GestureInfo(action, label.getText().toString());
            } else {
                info.setIntent(action);
                info.setLabel(label.getText().toString());
            }
            info.setGesture(this, gesture);
            data.putExtra(GESTURE, info);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
