package com.faendir.lightning_launcher.multitool.viewcreator;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.FileManagerFactory;
import com.faendir.lightning_launcher.multitool.util.ListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewCreatorFragment extends Fragment {

    private ListView listView;
    private FileManager<CustomView> fileManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileManager = FileManagerFactory.createCustomViewFileManager(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listView = new ListView(getActivity());
        List<CustomView> customViews = fileManager.read();
        if (customViews == null) customViews = new ArrayList<>();
        ListAdapter<CustomView> adapter = new ListAdapter<>(getActivity(),customViews);
        listView.setAdapter(adapter);
        return listView;
    }

}
