package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.multitool.settings.PrefsFragment;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.executor.DirectScriptExecutor;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.List;

import java8.util.stream.StreamSupport;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;

/**
 * Created by Lukas on 22.08.2015.
 * Main activity of ScriptManager
 */
public class ScriptManagerFragment extends Fragment {

    private SharedPreferences sharedPref;
    private boolean enableMenu;
    private FrameLayout layout;
    private ListManager listManager;
    private ScriptManager scriptManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scriptManager = new ScriptManager(getActivity());
        if(DEBUG) scriptManager.enableDebug();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        listManager = new ListManager(scriptManager, getActivity());
        setHasOptionsMenu(true);
        enableMenu = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = new FrameLayout(getActivity());
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        loadFromLauncher();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_scriptmanager, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!enableMenu) {
            Toast.makeText(getActivity(), R.string.toast_menuDisabled, Toast.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_restore:
                Intent intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                intent.putExtra(FilePickerActivity.EXTRA_START_PATH, sharedPref.getString(getString(R.string.pref_directory), PrefsFragment.DEFAULT_BACKUP_PATH));
                startActivityForResult(intent, 0);
                break;
            case R.id.action_search:
                ScriptUtils.searchDialog(getActivity(), listManager);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            StreamSupport.stream(Utils.getFilePickerActivityResult(data)).forEach(uri -> ScriptUtils.restoreFromFile(scriptManager, getActivity(), listManager, uri));
        }
    }

    private void loadFromLauncher() {
        scriptManager.getAsyncExecutorService().setKeepAliveAfterwards(true).add(new DirectScriptExecutor(R.raw.scriptmanager).putVariable("data", null), this::handleScriptResult).start();
        layout.removeAllViews();
        LayoutInflater.from(getActivity()).inflate(R.layout.fragment_loading, layout);
    }

    private void handleScriptResult(@Nullable String result) {
        if (result != null) {
            List<Script> scripts = Arrays.asList(Utils.GSON.fromJson(result, Script[].class));
            listManager.updateFrom(scripts);
            listManager.setAsContentOf(layout);
            enableMenu = true;
        }
    }

    @Subscribe
    public void onReloadButton(ClickEvent event) {
        if (event.getId() == R.id.button_reload) {
            sharedPref.edit().putInt(getString(R.string.pref_id), -1).apply();
            loadFromLauncher();
        }
    }
}
