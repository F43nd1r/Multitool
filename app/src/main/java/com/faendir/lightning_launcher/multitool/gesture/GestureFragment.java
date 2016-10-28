package com.faendir.lightning_launcher.multitool.gesture;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.FileManagerFactory;
import com.faendir.lightning_launcher.multitool.util.ListAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GestureFragment extends Fragment implements ListView.MultiChoiceModeListener, AdapterView.OnItemClickListener {

    private static final int ADD = 1;
    static final int EDIT = 2;
    static final String INDEX = "index";

    private ListAdapter<GestureInfo> adapter;
    private FileManager<GestureInfo> fileManager;
    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        fileManager = FileManagerFactory.createGestureFileManager(getActivity());
        fileManager.allowGlobalRead();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        listView = new ListView(getActivity());
        List<GestureInfo> gestureInfos = fileManager.read();
        if (gestureInfos == null) gestureInfos = new ArrayList<>();
        for (Iterator<GestureInfo> iterator = gestureInfos.iterator(); iterator.hasNext(); ) {
            if (iterator.next().isInvalid()) {
                iterator.remove();
            }
        }
        adapter = new ListAdapter<>(getActivity(), gestureInfos);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
        listView.setOnItemClickListener(this);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView empty = (TextView) inflater.inflate(R.layout.textview_empty_gestures_list, listView, false);
        listView.setEmptyView(empty);
        layout.addView(listView);
        layout.addView(empty);
        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gesture, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_gesture:
                startActivityForResult(new Intent(getActivity(), GestureActivity.class), ADD);
                return true;
            case R.id.action_help:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_help)
                        .setMessage(R.string.message_helpGesture)
                        .setPositiveButton(R.string.button_ok, null)
                        .show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ADD: {
                    GestureInfo gestureInfo = data.getParcelableExtra(GestureActivity.GESTURE);
                    adapter.add(gestureInfo);
                    adapter.notifyDataSetChanged();
                    GestureUtils.updateSavedGestures(adapter, fileManager);
                    break;
                }
                case EDIT: {
                    GestureInfo gestureInfo = data.getParcelableExtra(GestureActivity.GESTURE);
                    int position = data.getIntExtra(INDEX, -1);
                    if (position >= 0) {
                        adapter.remove(adapter.getItem(position));
                        adapter.insert(gestureInfo, position);
                        adapter.notifyDataSetChanged();
                        GestureUtils.updateSavedGestures(adapter, fileManager);
                    }
                }
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_context_gesture, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        //if (listView.getCheckedItemCount() == 0) mode.finish();
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ArrayList<GestureInfo> selected = new ArrayList<>();
        SparseBooleanArray array = listView.getCheckedItemPositions();
        for (int i = 0; i < array.size(); i++) {
            if (array.valueAt(i)) {
                selected.add(adapter.getItem(array.keyAt(i)));
            }
        }
        switch (item.getItemId()) {
            case R.id.action_edit:
                GestureUtils.edit(getActivity(), selected.get(0), adapter);
                break;
            case R.id.action_delete:
                GestureUtils.deleteDialog(getActivity(), selected, adapter, fileManager);
        }
        listView.clearChoices();
        adapter.clearSelection();
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        adapter.setSelection(position, checked);
        switch (listView.getCheckedItemCount()) {
            case 0:
                mode.finish();
                break;
            case 1:
                mode.getMenu().findItem(R.id.action_edit).setVisible(true);
                mode.invalidate();
                break;
            default:
                if (mode.getMenu().findItem(R.id.action_edit).isVisible()) {
                    mode.getMenu().findItem(R.id.action_edit).setVisible(false);
                    mode.invalidate();
                }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean select = !adapter.isSelected(position);
        adapter.setSelection(position, select);
        listView.setItemChecked(position, select);
        adapter.notifyDataSetChanged();
    }
}
