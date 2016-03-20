package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.BuildConfig;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.SettingsActivity;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.FileManagerFactory;
import com.faendir.lightning_launcher.scriptlib.ErrorCode;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Lukas on 22.08.2015.
 * Main activity of ScriptManager
 */
public class ScriptManagerFragment extends Fragment implements ActionMode.Callback {

    private SharedPreferences sharedPref;
    private FileManager<ScriptGroup> fileManager;
    private List<Script> scripts;
    private List<ScriptGroup> items;
    private ScriptListAdapter adapter;
    private ActionMode mode;
    private ExpandableListView listView;
    private FrameLayout layout;
    private Parcelable listViewState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        fileManager = FileManagerFactory.createScriptFileManager(getActivity());
        listView = new ExpandableListView(getActivity());
        listView.setDrawSelectorOnTop(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long packedPos = listView.getExpandableListPosition(position);
                toggleItem(packedPos);
                return true;
            }
        });
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                long packedPos = ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
                toggleItem(packedPos);
                return true;
            }
        });
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                long packedPos = ExpandableListView.getPackedPositionForGroup(groupPosition);
                if (adapter.isSelected(packedPos)) {
                    selectItem(packedPos, false);
                    return true;
                } else if (parent.isGroupExpanded(groupPosition)) {
                    for (int i = adapter.getChildrenCount(groupPosition) - 1; i >= 0; i--) {
                        selectItem(ExpandableListView.getPackedPositionForChild(groupPosition, i), false);
                    }
                }
                return false;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.key_listView))) {
            listView.onRestoreInstanceState(savedInstanceState.getParcelable(getString(R.string.key_listView)));
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getString(R.string.key_listView), listView.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = new FrameLayout(getActivity());
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_scriptManager);
        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        onNewIntent(getActivity().getIntent());
    }

    private void toggleItem(long packedPos) {
        selectItem(packedPos, !adapter.isSelected(packedPos));
    }

    private void selectItem(long packedPos, boolean select) {
        adapter.select(packedPos, select);
        if (adapter.getSelectedPackedPosition().size() > 0 && mode == null) {
            mode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
        } else if (adapter.getSelectedPackedPosition().size() > 0) {
            mode.invalidate();
        } else if (mode != null) {
            mode.finish();
        }
        adapter.notifyDataSetChanged();

    }

    public void onNewIntent(Intent intent) {
        if (intent.hasExtra(getString(R.string.extra_scripts))) {
            String scriptStrings = intent.getStringExtra(getString(R.string.extra_scripts));
            scripts = Arrays.asList(ScriptUtils.GSON.fromJson(scriptStrings, Script[].class));
            ArrayList<Script> existing = new ArrayList<>();
            if (items == null) {
                items = new ArrayList<>();
                ScriptGroup def = new ScriptGroup(getString(R.string.text_defaultScriptGroup), false);
                items.add(def);
                for (Script s : scripts) {
                    def.add(s);
                }
            }
            ScriptGroup def = null;
            for (ScriptGroup i : items) {
                for (Iterator<Script> it = i.iterator(); it.hasNext(); ) {
                    Script s = it.next();
                    if (!checkIfStillExisting(s)) it.remove();
                    else existing.add(s);
                }
                if (!i.allowsDelete()) def = i;
            }
            assert def != null;
            for (Script s : scripts) {
                if (!existing.contains(s)) {
                    def.add(s);
                }
            }
            adapter = new ScriptListAdapter(getActivity(), items, listView);
            listView.setAdapter(adapter);
            layout.removeAllViews();
            layout.addView(listView);
        } else {
            int id = sharedPref.getInt(getString(R.string.pref_id), -1);
            loadFromLauncher(id);
        }
    }

    private void loadFromLauncher(int id) {
        if (id == -1 || sharedPref.getInt(getString(R.string.pref_version), 0) != BuildConfig.VERSION_CODE) {
            // preload strings, because they can't be loaded anymore, if the fragment is detached
            final String idString = getString(R.string.pref_id);
            final String versionString = getString(R.string.pref_version);
            ScriptManager.loadScript(getActivity(), new com.trianguloy.llscript.repository.aidl.Script(getActivity(), R.raw.scriptmanager, getString(R.string.text_scriptTitle), 0),
                    new ScriptManager.Listener() {
                        @Override
                        public void onError(ErrorCode errorCode) {
                            if (errorCode == ErrorCode.SECURITY_EXCEPTION) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle(R.string.title_error)
                                        .setMessage(R.string.error_securityException)
                                        .setPositiveButton(R.string.button_ok, null)
                                        .show();
                            }
                        }

                        @Override
                        public void onLoadFinished(int i) {
                            sharedPref.edit().putInt(idString, i).putInt(versionString, BuildConfig.VERSION_CODE).apply();
                            try {
                                ScriptManager.runScript(getActivity(), i, null, true);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    }, true);
        } else {
            ScriptManager.runScript(getActivity(), id, null, true);
        }
        items = fileManager.read();
        layout.removeAllViews();
        LayoutInflater.from(getActivity()).inflate(R.layout.fragment_loading, layout);

    }

    private boolean checkIfStillExisting(Script item) {
        if (scripts.contains(item)) {
            Script script = scripts.get(scripts.indexOf(item));
            item.fillFrom(script);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (items != null) fileManager.write(items);
        listViewState = listView.onSaveInstanceState();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listViewState != null) listView.onRestoreInstanceState(listViewState);
        if (mode != null) mode.invalidate();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_scriptmanager, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (scripts == null) {
            Toast.makeText(getActivity(), R.string.toast_menuDisabled, Toast.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_add_group:
                ScriptUtils.createGroupDialog(getActivity(), items, adapter);
                break;
            case R.id.action_restore:
                Intent intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                intent.putExtra(FilePickerActivity.EXTRA_START_PATH, sharedPref.getString(getString(R.string.pref_directory), SettingsActivity.DEFAULT_BACKUP_PATH));
                startActivityForResult(intent, 0);
                break;
            case R.id.action_search:
                ScriptUtils.searchDialog(getActivity(), items);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_context_scriptmanager, menu);
        onPrepareActionMode(mode, menu);
        return true;
    }

    @IntDef({NONE, ONE_SCRIPT, ONE_GROUP, ONLY_GROUPS, ONLY_SCRIPTS, BOTH})
    @interface SelectionMode {
    }

    private static final int NONE = -1;
    private static final int ONE_SCRIPT = 0;
    private static final int ONE_GROUP = 1;
    private static final int ONLY_SCRIPTS = 2;
    private static final int ONLY_GROUPS = 3;
    private static final int BOTH = 4;

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int selectionMode = getSelectionMode();
        if (selectionMode == NONE) mode.finish();
        menu.findItem(R.id.action_rename).setVisible(selectionMode == ONE_GROUP || selectionMode == ONE_SCRIPT);
        menu.findItem(R.id.action_delete).setVisible(true);
        menu.findItem(R.id.action_move_to_group).setVisible(selectionMode == ONLY_SCRIPTS || selectionMode == ONE_SCRIPT);
        menu.findItem(R.id.action_edit).setVisible(selectionMode == ONE_SCRIPT);
        menu.findItem(R.id.action_backup).setVisible(selectionMode == ONLY_SCRIPTS || selectionMode == ONE_SCRIPT);
        menu.findItem(R.id.action_format).setVisible(selectionMode == ONLY_SCRIPTS || selectionMode == ONE_SCRIPT);
        return true;
    }

    @SelectionMode
    private int getSelectionMode() {
        int selectionMode = NONE;
        List<Long> checked = adapter.getSelectedPackedPosition();
        if (checked.size() == 1) {
            int type = ExpandableListView.getPackedPositionType(checked.get(0));
            selectionMode = type == ExpandableListView.PACKED_POSITION_TYPE_GROUP ? ONE_GROUP : ONE_SCRIPT;
        } else {
            for (long position : checked) {
                int type = ExpandableListView.getPackedPositionType(position);
                boolean isGroup = type == ExpandableListView.PACKED_POSITION_TYPE_GROUP;
                if (selectionMode == NONE) {
                    selectionMode = isGroup ? ONLY_GROUPS : ONLY_SCRIPTS;
                } else if ((selectionMode == ONLY_GROUPS && !isGroup) || (selectionMode == ONLY_SCRIPTS && isGroup)) {
                    selectionMode = BOTH;
                    break;
                }
            }
        }
        return selectionMode;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        List<Long> selection = adapter.getSelectedPackedPosition();
        List<ScriptItem> selectedItems = new ArrayList<>();
        for (Long l : selection) {
            int type = ExpandableListView.getPackedPositionType(l);
            if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                selectedItems.add(items.get(ExpandableListView.getPackedPositionGroup(l)));
            } else {
                selectedItems.add(items.get(ExpandableListView.getPackedPositionGroup(l)).get(ExpandableListView.getPackedPositionChild(l)));
            }
        }
        switch (item.getItemId()) {
            case R.id.action_rename:
                ScriptUtils.renameDialog(getActivity(), adapter, selectedItems.get(0));
                break;
            case R.id.action_delete:
                ScriptUtils.deleteDialog(getActivity(), items, adapter, selectedItems);
                break;
            case R.id.action_move_to_group:
                ScriptUtils.moveDialog(getActivity(), items, adapter, selectedItems);
                break;
            case R.id.action_edit:
                ScriptUtils.editScript(getActivity(), adapter, (Script) selectedItems.get(0));
                break;
            case R.id.action_backup:
                ScriptUtils.backup(getActivity(), adapter, selectedItems);
                break;
            case R.id.action_format:
                ScriptUtils.format(getActivity(), adapter, selectedItems);
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        adapter.deselectAll();
        adapter.notifyDataSetChanged();
        this.mode = null;
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
                            ScriptUtils.restoreFromFile(getActivity(), items, uri);
                        }
                    }
                    // For ICS
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            ScriptUtils.restoreFromFile(getActivity(), items, uri);
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                ScriptUtils.restoreFromFile(getActivity(), items, uri);
            }
        }
    }

    public void onReloadButton() {
        sharedPref.edit().putInt(getString(R.string.pref_id), -1).apply();
        loadFromLauncher(-1);
    }
}
