package com.faendir.lightning_launcher.multitool.gesture;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.FileManagerFactory;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.omniadapter.OmniAdapter;
import com.faendir.omniadapter.OmniBuilder;
import com.faendir.omniadapter.model.Action;
import com.faendir.omniadapter.model.ChangeInformation;
import com.faendir.omniadapter.model.Component;
import com.faendir.omniadapter.model.DeepObservableList;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * A simple {@link Fragment} subclass.
 */
public class GestureFragment extends Fragment implements OmniAdapter.Controller<GestureInfo>, OmniAdapter.UndoListener<GestureInfo>, Action.LongClick.Listener {

    private static final int ADD = 1;
    private static final int EDIT = 2;
    private static final int EXPORT = 3;
    private static final int IMPORT = 4;
    private static final String INDEX = "index";

    private FileManager<GestureInfo> fileManager;
    private DeepObservableList<GestureInfo> gestureInfos;

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
        RecyclerView recyclerView = new RecyclerView(getActivity());
        gestureInfos = DeepObservableList.copyOf(GestureInfo.class,
                StreamSupport.stream(fileManager.read()).filter(gestureInfo -> !gestureInfo.isInvalid()).collect(Collectors.toList()));
        new OmniBuilder<>(getActivity(), gestureInfos, this)
                .setLongClick(new Action.LongClick(Action.CUSTOM, this))
                .setSwipeToRight(new Action.Swipe(Action.REMOVE))
                .enableUndoForAction(Action.REMOVE, R.string.text_itemRemoved)
                .addUndoListener(this)
                .attach(recyclerView);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView empty = (TextView) inflater.inflate(R.layout.textview_empty_gestures_list, recyclerView, false);
        layout.addView(recyclerView);
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
                break;
            case R.id.action_help:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_help)
                        .setMessage(R.string.message_helpGesture)
                        .setPositiveButton(R.string.button_ok, null)
                        .show();
                break;
            case R.id.action_export: {
                Intent intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                startActivityForResult(intent, EXPORT);
                break;
            }
            case R.id.action_import: {
                Intent intent = new Intent(getActivity(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                startActivityForResult(intent, IMPORT);
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ADD: {
                    GestureInfo gestureInfo = data.getParcelableExtra(GestureActivity.GESTURE);
                    gestureInfos.add(gestureInfo);
                    GestureUtils.updateSavedGestures(gestureInfos, fileManager);
                    break;
                }
                case EDIT: {
                    GestureInfo gestureInfo = data.getParcelableExtra(GestureActivity.GESTURE);
                    int position = data.getIntExtra(INDEX, -1);
                    if (position >= 0) {
                        gestureInfos.set(position, gestureInfo);
                        GestureUtils.updateSavedGestures(gestureInfos, fileManager);
                    }
                }
                case EXPORT:
                    StreamSupport.stream(Utils.getFilePickerActivityResult(data)).findAny()
                            .ifPresent(uri -> GestureUtils.exportGestures(getActivity(), uri, fileManager));
                    break;
                case IMPORT:
                    StreamSupport.stream(Utils.getFilePickerActivityResult(data)).findAny()
                            .ifPresent(uri -> GestureUtils.importGestures(getActivity(), uri, gestureInfos, fileManager));
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public View createView(ViewGroup parent, int level) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.list_item_app, parent, false);
    }

    @Override
    public void bindView(View view, GestureInfo component, int level) {
        final TextView txt = (TextView) view;
        txt.setText(component.getText());
        Drawable img = component.getImage(getActivity());
        txt.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }

    @Override
    public boolean shouldMove(GestureInfo component, DeepObservableList from, int fromPosition, DeepObservableList to, int toPosition) {
        return false;
    }

    @Override
    public boolean isSelectable(GestureInfo component) {
        return true;
    }

    @Override
    public boolean shouldSwipe(GestureInfo component, int direction) {
        return true;
    }

    @Override
    public void onActionPersisted(List<? extends ChangeInformation<GestureInfo>> changes) {
        GestureUtils.delete(getActivity(), StreamSupport.stream(changes)
                        .filter(change -> change instanceof ChangeInformation.Remove)
                        .map(ChangeInformation::getComponent).collect(Collectors.toList()),
                gestureInfos, fileManager);
    }

    @Override
    public void onActionReverted(List<? extends ChangeInformation<GestureInfo>> changes) {
    }

    @Override
    public boolean allowLongClick(Component component, int action) {
        return true;
    }

    @Override
    public void onLongClick(Component component, int action) {
        Intent intent = new Intent(getActivity(), GestureActivity.class);
        intent.putExtra(GestureActivity.GESTURE, (GestureInfo)component);
        intent.putExtra(INDEX, gestureInfos.indexOf(component));
        startActivityForResult(intent, EDIT);
    }
}
