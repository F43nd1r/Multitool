package com.faendir.lightning_launcher.multitool.launcherscript;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.multitool.util.Utils;
import net.pierrox.lightning_launcher.api.Script;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class LauncherScriptFragment extends Fragment {

    private TextView nameTextView;
    private SharedPreferences shareprefs;
    private FrameLayout layout;

    private Button importButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_launcher_script, container);
        nameTextView = layout.findViewById(R.id.main_scriptName);
        importButton = layout.findViewById(R.id.button_import);
        shareprefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        nameTextView.setText(shareprefs.getString(getString(R.string.preference_scriptName), getString(R.string.script_name)));
        return v;
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
        if (event.getId() == R.id.button_import) {
            saveName();
            importButton.setText(getString(R.string.button_repositoryImporter_importing));
            MultiTool.get().doInLL(scriptService -> {
                scriptService.updateScript(new Script(Utils.readRawResource(getActivity(), R.raw.multitool), nameTextView.getText().toString(), getActivity().getPackageName(), Script.FLAG_APP_MENU | Script.FLAG_ITEM_MENU));
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> importButton.setText(getString(R.string.button_repositoryImporter_importOk)));
                }
            });
        }

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
