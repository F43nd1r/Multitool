package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
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
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.FileManagerFactory;
import com.faendir.lightning_launcher.scriptlib.ResultCallback;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.executor.DirectScriptExecutor;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Lukas on 22.08.2015.
 * Main activity of ScriptManager
 */
public class ScriptManagerFragment extends Fragment {

    private SharedPreferences sharedPref;
    private FileManager<ScriptGroup> fileManager;
    private boolean enableMenu;
    private FrameLayout layout;
    private ListManager listManager;
    private ScriptManager scriptManager;
    private boolean stopAutoLoad = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scriptManager = new ScriptManager(getActivity());
        scriptManager.enableDebug();
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (scriptManager.bind()) {
                    case OK:
                        break;
                    default:
                        stopAutoLoad = true;
                        break;
                }
            }
        }).start();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        fileManager = FileManagerFactory.createScriptFileManager(getActivity());
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
        listManager.restoreFrom(fileManager);
        loadFromLauncher();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        if (!stopAutoLoad) {
            scriptManager.unbind();
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        listManager.saveTo(fileManager);
        super.onPause();
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
            case R.id.action_add_group:
                ScriptUtils.createGroupDialog(getActivity(), listManager);
                break;
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
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            ScriptUtils.restoreFromFile(scriptManager, getActivity(), listManager, uri);
                        }
                    }
                    // For ICS
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            ScriptUtils.restoreFromFile(scriptManager, getActivity(), listManager, uri);
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                ScriptUtils.restoreFromFile(scriptManager, getActivity(), listManager, uri);
            }
        }
    }

    private void loadFromLauncher() {
        scriptManager.getAsyncExecutorService().setKeepAliveAfterwards(true).add(new DirectScriptExecutor(R.raw.scriptmanager).putVariable("data", null), new ResultCallback<String>() {
            @Override
            public void onResult(String result) {
                handleScriptResult(result);
            }
        }).start();
        layout.removeAllViews();
        LayoutInflater.from(getActivity()).inflate(R.layout.fragment_loading, layout);
    }

    private void handleScriptResult(@Nullable String result) {
        if (result != null) {
            List<Script> scripts = Arrays.asList(ScriptUtils.GSON.fromJson(result, Script[].class));
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
