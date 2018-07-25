package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.multitool.fastadapter.Model;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import java9.util.stream.StreamSupport;
import org.acra.ACRA;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;

/**
 * Created by Lukas on 22.08.2015.
 * Main activity of ScriptManager
 */
public class ScriptManagerFragment extends Fragment implements ActionMode.Callback {
    private static final int IMPORT = 1;
    private static final int EXPORT = 2;
    private SharedPreferences sharedPref;
    private boolean enableMenu;
    private FrameLayout layout;
    private ListManager listManager;
    private ScriptManager scriptManager;
    private ActionMode actionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scriptManager = new ScriptManager(getActivity());
        if (DEBUG) scriptManager.enableDebug();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        listManager = new ListManager(scriptManager, getActivity(), this::setActionModeEnabled);
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
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, IMPORT);
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
            switch (requestCode) {
                case IMPORT:
                    if (data != null && data.getData() != null) {
                        ScriptUtils.restoreFromFile(scriptManager, getActivity(), listManager, data.getData());
                    }
                    break;
                case EXPORT:
                    if (data != null && data.getData() != null) {
                        ScriptUtils.backup(getActivity(), listManager, (Script) listManager.getSelectedItems().get(0), data.getData());
                    }
            }
        }
    }

    private void loadFromLauncher() {
        scriptManager.getAsyncExecutorService().setKeepAliveAfterwards(true).add(ScriptUtils.getScriptManagerExecutor(null), this::handleScriptResult).start();
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

    private void setActionModeEnabled(boolean enable) {
        if (enable) {
            if (actionMode == null) {
                actionMode = getActivity().startActionMode(this);
            } else {
                actionMode.invalidate();
            }
        } else if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
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
        final boolean isOneScript = selectionMode == ListManager.ONE_SCRIPT;
        final boolean onlyScripts = isOneScript || selectionMode == ListManager.ONLY_SCRIPTS;
        menu.findItem(R.id.action_rename).setVisible(selectionMode == ListManager.ONE_GROUP || isOneScript);
        menu.findItem(R.id.action_edit).setVisible(isOneScript);
        menu.findItem(R.id.action_backup).setVisible(isOneScript);
        menu.findItem(R.id.action_format).setVisible(onlyScripts);
        MenuItem disable = menu.findItem(R.id.action_disable).setVisible(isOneScript);
        if (isOneScript) {
            disable.setTitle(((Script) StreamSupport.stream(listManager.getSelectedItems()).findAny().get()).isDisabled() ? R.string.menu_enable : R.string.menu_disable);
        }
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<Model> selectedItems = new ArrayList<>(listManager.getSelectedItems());
        if (selectedItems.isEmpty()) {
            ACRA.getErrorReporter().putCustomData("listManager", listManager.toString());
            ACRA.getErrorReporter().handleSilentException(new IllegalStateException("No selected items"));
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_rename:
                ScriptUtils.renameDialog(scriptManager, getActivity(), listManager, selectedItems.get(0));
                break;
            case R.id.action_edit:
                ScriptUtils.editScript(getActivity(), listManager, (Script) selectedItems.get(0));
                break;
            case R.id.action_backup:
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/javascript");
                Script script = (Script) selectedItems.get(0);
                intent.putExtra(Intent.EXTRA_TITLE, script.getId() + "_" + script.getName().replace("[,\\./\\:*?\"<>\\|]", "_") + ".js");
                startActivityForResult(intent, EXPORT);
                break;
            case R.id.action_format:
                ScriptUtils.format(scriptManager, getActivity(), listManager, selectedItems);
                break;
            case R.id.action_disable:
                ScriptUtils.toggleDisable(scriptManager, listManager, (Script) selectedItems.get(0));
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        listManager.deselectAll();
    }
}
