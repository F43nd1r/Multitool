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
import android.support.v7.view.ActionMode;
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
import com.faendir.lightning_launcher.multitool.event.UpdateActionModeRequest;
import com.faendir.lightning_launcher.multitool.settings.PrefsFragment;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.FileManagerFactory;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.exception.RepositoryImporterException;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Lukas on 22.08.2015.
 * Main activity of ScriptManager
 */
public class ScriptManagerFragment extends Fragment implements ActionMode.Callback, ListManager.ClickListener {

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scriptManager.bind();
                } catch (RepositoryImporterException e) {
                    e.printStackTrace();
                    stopAutoLoad = true;
                }
            }
        }).start();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        fileManager = FileManagerFactory.createScriptFileManager(getActivity());
        listManager = new ListManager(scriptManager, getActivity(), this);
        listManager.restoreFrom(savedInstanceState);
        setHasOptionsMenu(true);
        enableMenu = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        listManager.saveTo(outState);
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
        listManager.save();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        listManager.restore();
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

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_context_scriptmanager, menu);
        onPrepareActionMode(mode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int selectionMode = listManager.getSelectionMode();
        if (selectionMode == ListManager.NONE) mode.finish();
        menu.findItem(R.id.action_rename).setVisible(selectionMode == ListManager.ONE_GROUP || selectionMode == ListManager.ONE_SCRIPT);
        menu.findItem(R.id.action_delete).setVisible(true);
        menu.findItem(R.id.action_move_to_group).setVisible(selectionMode == ListManager.ONLY_SCRIPTS || selectionMode == ListManager.ONE_SCRIPT);
        menu.findItem(R.id.action_edit).setVisible(selectionMode == ListManager.ONE_SCRIPT);
        menu.findItem(R.id.action_backup).setVisible(selectionMode == ListManager.ONLY_SCRIPTS || selectionMode == ListManager.ONE_SCRIPT);
        menu.findItem(R.id.action_format).setVisible(selectionMode == ListManager.ONLY_SCRIPTS || selectionMode == ListManager.ONE_SCRIPT);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<ScriptItem> selectedItems = listManager.getSelectedItems();
        if (selectedItems.isEmpty()) {
            ACRA.getErrorReporter().putCustomData("listManager", listManager.toString());
            ACRA.getErrorReporter().putCustomData("enableMenu", String.valueOf(enableMenu));
            ACRA.getErrorReporter().handleSilentException(new IllegalStateException("No selected items"));
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_rename:
                ScriptUtils.renameDialog(scriptManager, getActivity(), listManager, selectedItems.get(0));
                break;
            case R.id.action_delete:
                ScriptUtils.deleteDialog(getActivity(), listManager, selectedItems);
                break;
            case R.id.action_move_to_group:
                ScriptUtils.moveDialog(getActivity(), listManager, selectedItems);
                break;
            case R.id.action_edit:
                ScriptUtils.editScript(getActivity(), listManager, (Script) selectedItems.get(0));
                break;
            case R.id.action_backup:
                ScriptUtils.backup(getActivity(), listManager, selectedItems);
                break;
            case R.id.action_format:
                ScriptUtils.format(scriptManager, getActivity(), listManager, selectedItems);
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        listManager.deselectAll();
    }

    @Override
    public boolean onChildClick(long packedPos) {
        listManager.toggleItem(packedPos);
        EventBus.getDefault().post(new UpdateActionModeRequest(this, listManager.hasSelection()));
        return true;
    }

    @Override
    public boolean onChildLongClick(long packedPos) {
        return onChildClick(packedPos);
    }

    @Override
    public boolean onGroupClick(long packedPos) {
        boolean handled = false;
        if (listManager.isSelected(packedPos)) {
            listManager.toggleItem(packedPos);
            handled = true;
        } else {
            listManager.deselectChildren(packedPos);
        }
        EventBus.getDefault().post(new UpdateActionModeRequest(this, listManager.hasSelection()));
        return handled;
    }

    @Override
    public boolean onGroupLongClick(long packedPos) {
        listManager.toggleItem(packedPos);
        EventBus.getDefault().post(new UpdateActionModeRequest(this, listManager.hasSelection()));
        return true;
    }

    private void loadFromLauncher() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = scriptManager.runScriptForResult(R.raw.scriptmanager, null);
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
