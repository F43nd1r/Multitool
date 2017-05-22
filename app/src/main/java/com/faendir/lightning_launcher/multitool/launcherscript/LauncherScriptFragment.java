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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.scriptlib.BaseExceptionHandler;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.executor.ScriptLoader;
import com.trianguloy.llscript.repository.aidl.Script;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.exceptionToOptional;


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
        inflater.inflate(R.layout.fragment_launcher_script, layout);

        checkLauncher();
        checkImporter();

        //prepare variables
        nameTextView = (TextView) layout.findViewById(R.id.main_scriptName);
        importButton = (Button) layout.findViewById(R.id.button_import);
        shareprefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //set name text
        nameTextView.setText(shareprefs.getString(getString(R.string.preference_scriptName), getString(R.string.script_name)));
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
                ScriptManager manager = new ScriptManager(getActivity());
                if (MultiTool.DEBUG) manager.enableDebug();
                manager.getAsyncExecutorService(new BaseExceptionHandler(getActivity()) {
                    @Override
                    protected void onUnhandledException(Exception e) {
                        if (isAdded()) {
                            getActivity().runOnUiThread(() -> changeText(getString(R.string.button_repositoryImporter_importError)));
                        }
                    }
                })
                        .add(new ScriptLoader(new Script(getActivity(),
                                R.raw.multitool,
                                nameTextView.getText().toString(),
                                Script.FLAG_APP_MENU + Script.FLAG_ITEM_MENU,
                                getActivity().getPackageName().replace('.', '/'))), result -> {
                            if (isAdded()) {
                                getActivity().runOnUiThread(() -> changeText(getString(R.string.button_repositoryImporter_importOk)));
                            }
                        })
                        .start();
                break;
            }

        }

    }


    private boolean isPackageInstalled(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        return exceptionToOptional(pm::getPackageInfo).apply(packageName, PackageManager.GET_ACTIVITIES).isPresent();
    }

    private void changeText(String newText) {
        importButton.setText(newText);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


}
