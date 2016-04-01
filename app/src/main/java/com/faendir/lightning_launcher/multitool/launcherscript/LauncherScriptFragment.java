package com.faendir.lightning_launcher.multitool.launcherscript;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.scriptlib.ErrorCode;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.trianguloy.llscript.repository.aidl.Script;

import org.greenrobot.eventbus.Subscribe;


public class LauncherScriptFragment extends Fragment {

    private TextView nameTextView;
    private SharedPreferences shareprefs;
    private FrameLayout layout;

    private Button importButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //populate
        layout = new FrameLayout(getActivity());
        LayoutInflater.from(getActivity()).inflate(R.layout.fragment_launcher_script, layout);

        checkLauncher();
        checkImporter();

        //prepare variables
        nameTextView = (TextView) layout.findViewById(R.id.main_scriptName);
        importButton = (Button) layout.findViewById(R.id.button_import);
        shareprefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        //set name text
        nameTextView.setText(
                shareprefs.getString(
                        getString(R.string.preference_scriptName),
                        getString(R.string.script_name)
                )
        );
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_launcherScript);
        return layout;
    }

    //checks if the importer app is installed
    private void checkImporter() {
        if (isPackageInstalled("com.trianguloy.llscript.repository", getActivity())) {
            layout.findViewById(R.id.view_repositoryImporter).setVisibility(View.VISIBLE);
        }
    }

    //checks if lightning launcher is installed and shows the alert view
    private void checkLauncher() {
        if (!isPackageInstalled("net.pierrox.lightning_launcher_extreme", getActivity())
                &&
                !isPackageInstalled("net.pierrox.lightning_launcher", getActivity())
                ) {
            layout.findViewById(R.id.view_noLauncher).setVisibility(View.VISIBLE);
            layout.findViewById(R.id.view_yesLauncher).setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveName();
    }

    private void saveName() {
        //save the name preference
        shareprefs.edit().putString(getString(R.string.preference_scriptName), nameTextView.getText().toString()).apply();
    }


    @Subscribe
    public void onButtonClick(ClickEvent event) {
        switch (event.getId()) {
            case R.id.button_code: {
                Intent intent = new Intent(getActivity(), Code.class);
                startActivity(intent);
                break;
            }
            case R.id.button_noLauncher: {
                final String appPackageName = "net.pierrox.lightning_launcher";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            }
            case R.id.button_import: {
                saveName();
                changeText(getString(R.string.button_repositoryImporter_importing));

                ScriptManager.loadScript(getActivity(),
                        new Script(getActivity(),
                                R.raw.multitool,
                                nameTextView.getText().toString(),
                                Script.FLAG_APP_MENU + Script.FLAG_ITEM_MENU),
                        new managerListener()
                );
                break;
            }

        }
    }


    private boolean isPackageInstalled(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void changeText(String newText) {
        importButton.setText(newText);
    }


    private class managerListener extends ScriptManager.Listener {
        @Override
        public void onLoadFinished(int i) {
            if(isAdded()) {
                changeText(getString(R.string.button_repositoryImporter_importOk));
            }
        }

        @Override
        public void onError(ErrorCode errorCode) {
            if (isAdded()) {
                changeText(getString(R.string.button_repositoryImporter_importError));
            }
        }
    }


}
