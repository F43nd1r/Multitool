package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.launcherscript.Constants;

public class LoadGesture extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data = new Intent();
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_ID, R.raw.gesture_setup);
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_NAME,getString(R.string.title_gestureLauncher));
        data.putExtra(Constants.INTENT_EXTRA_EXECUTE_ON_LOAD, true);
        data.putExtra(Constants.INTENT_EXTRA_DELETE_AFTER_EXECUTION, true);
        setResult(RESULT_OK, data);
        finish();
    }
}
