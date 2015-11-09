package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.app.lukas.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.multitool.BuildConfig;
import com.faendir.lightning_launcher.multitool.R;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Lukas on 22.08.2015.
 * Main activity of ScriptManager
 */
public class ScriptManagerActivity extends AppCompatActivity implements ActionMode.Callback {

    private SharedPreferences sharedPref;
    private FileManager fileManager;
    private List<Script> scripts;
    private List<ScriptGroup> items;
    private ScriptListAdapter adapter;
    private ActionMode mode;
    private ExpandableListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        fileManager = new FileManager(this);
        listView = new ExpandableListView(this);
        onNewIntent(getIntent());
        listView.setDrawSelectorOnTop(true);
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
    }

    private void toggleItem(long packedPos) {
        selectItem(packedPos, !adapter.isSelected(packedPos));
    }

    private void selectItem(long packedPos, boolean select) {
        adapter.select(packedPos, select);
        if (adapter.getSelectedPackedPosition().size() > 0 && mode == null) {
            mode = startActionMode(this);
        } else if (adapter.getSelectedPackedPosition().size() > 0) {
            mode.invalidate();
        } else if (mode != null) {
            mode.finish();
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
                for (Script s : i) {
                    if (!checkIfStillExisting(s)) i.remove(s);
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
            adapter = new ScriptListAdapter(this, items, listView);
            listView.setAdapter(adapter);
            setContentView(listView);
        } else {
            int id = sharedPref.getInt(getString(R.string.pref_id), -1);
            loadFromLauncher(id);
        }
    }

    private void loadFromLauncher(int id){
        if (BuildConfig.DEBUG || id == -1 || sharedPref.getInt(getString(R.string.pref_version), 0) != BuildConfig.VERSION_CODE) {
            try {
                ScriptManager.loadScript(this, R.raw.scriptmanager, getString(R.string.text_scriptTitle), 0, true, new ScriptManager.Listener() {
                    @Override
                    public void OnLoadFinished(int i) {
                        sharedPref.edit().putInt(getString(R.string.pref_id), i).putInt(getString(R.string.pref_version), BuildConfig.VERSION_CODE).apply();
                        ScriptManager.runScript(ScriptManagerActivity.this, i, null, true);
                    }
                });
                overridePendingTransition(0,0);
            } catch (IOException e) {
                throw new FileManager.FatalFileException(e);
            }
        } else {
            ScriptManager.runScript(this, id, null, true);
        }
        items = fileManager.read();
        setContentView(R.layout.activity_loading);

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
    protected void onPause() {
        super.onPause();
        if (items != null) fileManager.write(items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scriptmanager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(scripts == null){
            Toast.makeText(this, R.string.toast_menuDisabled, Toast.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_add_group:
                ScriptUtils.createGroupDialog(this,items,adapter);
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_restore:
                Intent intent = new Intent(this, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                intent.putExtra(FilePickerActivity.EXTRA_START_PATH, sharedPref.getString(getString(R.string.pref_directory), SettingsActivity.DEFAULT_BACKUP_PATH));
                startActivityForResult(intent, 0);
                break;
            case R.id.action_search:
                ScriptUtils.searchDialog(this,items);
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

    private static final int NONE = -1;
    private static final int ONE_SCRIPT = 0;
    private static final int ONE_GROUP = 1;
    private static final int ONLY_SCRIPTS = 2;
    private static final int ONLY_GROUPS = 3;
    private static final int BOTH = 4;

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int selectionMode = getSelectionMode();
        menu.findItem(R.id.action_rename).setVisible(selectionMode == ONE_GROUP || selectionMode == ONE_SCRIPT);
        menu.findItem(R.id.action_delete).setVisible(true);
        menu.findItem(R.id.action_move_to_group).setVisible(selectionMode == ONLY_SCRIPTS || selectionMode == ONE_SCRIPT);
        menu.findItem(R.id.action_edit).setVisible(selectionMode == ONE_SCRIPT);
        menu.findItem(R.id.action_backup).setVisible(selectionMode == ONLY_SCRIPTS || selectionMode == ONE_SCRIPT);
        menu.findItem(R.id.action_format).setVisible(selectionMode == ONLY_SCRIPTS || selectionMode == ONE_SCRIPT);
        return true;
    }

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
                ScriptUtils.renameDialog(this, adapter, selectedItems.get(0));
                break;
            case R.id.action_delete:
                ScriptUtils.deleteDialog(this,items,adapter,selectedItems);
                break;
            case R.id.action_move_to_group:
                ScriptUtils.moveDialog(this, items, adapter, selectedItems);
                break;
            case R.id.action_edit:
                ScriptUtils.editScript(this, adapter, (Script) selectedItems.get(0));
                break;
            case R.id.action_backup:
                ScriptUtils.backup(this, adapter, selectedItems);
                break;
            case R.id.action_format:
                ScriptUtils.format(this, adapter, selectedItems);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            ScriptUtils.restoreFromFile(this, items, uri);
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            ScriptUtils.restoreFromFile(this, items, uri);
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                ScriptUtils.restoreFromFile(this, items, uri);
            }
        }
    }

    public void onReloadButton(View ignored){
        sharedPref.edit().putInt(getString(R.string.pref_id),-1).apply();
        loadFromLauncher(-1);
    }
}
