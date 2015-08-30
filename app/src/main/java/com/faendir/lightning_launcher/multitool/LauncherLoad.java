package com.faendir.lightning_launcher.multitool;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class LauncherLoad extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String name = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.preference_scriptName),getString(R.string.script_name));

        Intent data = new Intent();
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_ID, R.raw.multitool);
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_NAME, name);
        data.putExtra(Constants.INTENT_EXTRA_SCRIPT_FLAGS,
                Constants.FLAG_APP_MENU+Constants.FLAG_ITEM_MENU);
        data.putExtra(Constants.INTENT_EXTRA_EXECUTE_ON_LOAD, false);
        data.putExtra(Constants.INTENT_EXTRA_DELETE_AFTER_EXECUTION, false);
        setResult(RESULT_OK, data);
        finish();
    }
}
