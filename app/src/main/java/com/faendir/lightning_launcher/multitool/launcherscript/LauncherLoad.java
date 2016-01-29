package com.faendir.lightning_launcher.multitool.launcherscript;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.faendir.lightning_launcher.multitool.R;

public class LauncherLoad extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data = new Intent();
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_ID, R.raw.multitool);
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_NAME, getString(R.string.script_name));
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_FLAGS,
                Constants.FLAG_APP_MENU+Constants.FLAG_ITEM_MENU);
        data.putExtra(Constants.INTENT_EXTRA_EXECUTE_ON_LOAD, false);
        data.putExtra(Constants.INTENT_EXTRA_DELETE_AFTER_EXECUTION, false);
        setResult(RESULT_OK, data);
        finish();
    }
}
