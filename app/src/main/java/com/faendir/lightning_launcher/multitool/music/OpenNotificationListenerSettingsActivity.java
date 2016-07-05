package com.faendir.lightning_launcher.multitool.music;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.faendir.lightning_launcher.multitool.R;

/**
 * Created by Lukas on 05.07.2016.
 */

public class OpenNotificationListenerSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_notification_listener_settings);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_yes:
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                //no break
            case R.id.button_no:
                finish();
                break;
        }
    }
}
