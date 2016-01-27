package com.faendir.lightning_launcher.multitool.gesture;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.FileManagerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GestureFragment extends Fragment {

    private static final int ADD = 1;

    private ImageListAdapter adapter;
    private FileManager<Gesture> fileManager;

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
        ListView listView = new ListView(getActivity());
        List<Gesture> gestures = fileManager.read();
        adapter = new ImageListAdapter(getActivity(), gestures);
        listView.setAdapter(adapter);
        return listView;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ADD:
                    Gesture gesture = data.getParcelableExtra(GestureActivity.GESTURE);
                    adapter.add(gesture);
                    adapter.notifyDataSetChanged();
                    updateSavedGestures();
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void updateSavedGestures(){
        ArrayList<Gesture> gestures = new ArrayList<>();
        for (int i = 0;i<adapter.getCount();i++){
            gestures.add((Gesture) adapter.getItem(i));
        }
        fileManager.write(gestures);
    }
}
