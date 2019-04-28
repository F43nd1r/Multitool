package com.faendir.lightning_launcher.multitool.gesture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem;
import com.faendir.lightning_launcher.multitool.fastadapter.ItemFactory;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class GestureFragment extends Fragment {
    private static final int ADD = 1;
    private static final int EDIT = 2;
    private static final int EXPORT = 3;
    private static final int IMPORT = 4;
    private static final String INDEX = "index";
    public static final String APPLICATION_ZIP = "application/zip";
    private ModelAdapter<GestureInfo, ExpandableItem<GestureInfo>> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = requireActivity();
        LinearLayout layout = new LinearLayout(context);
        RecyclerView recyclerView = new RecyclerView(context);
        adapter = new ModelAdapter<>(ItemFactory.<GestureInfo>forLauncherIconSize(context)::wrap);
        FastAdapter<ExpandableItem<GestureInfo>> fastAdapter = FastAdapter.with(adapter);
        fastAdapter.withOnLongClickListener((v, adapter, item, position) -> {
            Intent intent = new Intent(context, GestureActivity.class);
            intent.putExtra(GestureActivity.GESTURE, item.getModel());
            intent.putExtra(INDEX, adapter.getAdapterPosition(item));
            startActivityForResult(intent, EDIT);
            return true;
        });
        adapter.set(StreamSupport.stream(GestureUtils.readFromFile(context)).filter(GestureInfo::isValid).collect(Collectors.toList()));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(fastAdapter);
        new ItemTouchHelper(new SimpleSwipeCallback((position, direction) -> {
            final ExpandableItem<GestureInfo> item = adapter.getAdapterItem(position);
            final Runnable removeRunnable = () -> {
                item.setSwipedAction(null);
                int position1 = adapter.getAdapterPosition(item);
                if (position1 != RecyclerView.NO_POSITION) {
                    adapter.remove(position1);
                }
                GestureUtils.delete(context, item.getModel(), adapter.getModels());
            };
            recyclerView.postDelayed(removeRunnable, 5000);

            item.setSwipedAction(() -> {
                recyclerView.removeCallbacks(removeRunnable);
                item.setSwipedAction(null);
                int position2 = adapter.getAdapterPosition(item);
                if (position2 != RecyclerView.NO_POSITION) {
                    fastAdapter.notifyAdapterItemChanged(position2);
                }
            });

            fastAdapter.notifyAdapterItemChanged(position);
        }, null, ItemTouchHelper.RIGHT).withLeaveBehindSwipeRight(ContextCompat.getDrawable(context, R.drawable.ic_delete_white)).withBackgroundSwipeRight(Color.RED))
                .attachToRecyclerView(recyclerView);
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
                new AlertDialog.Builder(getActivity()).setTitle(R.string.title_help).setMessage(R.string.message_helpGesture).setPositiveButton(R.string.button_ok, null)
                        .show();
                break;
            case R.id.action_export: {
                Intent intent;
                intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(APPLICATION_ZIP);
                intent.putExtra(Intent.EXTRA_TITLE, "Multitool_Gestures_" + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ".zip");
                startActivityForResult(intent, EXPORT);
                break;
            }
            case R.id.action_import: {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(APPLICATION_ZIP);
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
                    adapter.add(gestureInfo);
                    GestureUtils.writeToFile(getActivity(), adapter.getModels());
                    break;
                }
                case EDIT: {
                    GestureInfo gestureInfo = data.getParcelableExtra(GestureActivity.GESTURE);
                    int position = data.getIntExtra(INDEX, -1);
                    if (position >= 0) {
                        adapter.set(position, gestureInfo);
                        GestureUtils.writeToFile(getActivity(), adapter.getModels());
                    }
                }
                case EXPORT:
                    if (data != null) {
                        final Uri uri = data.getData();
                        if (uri != null) {
                            GestureUtils.exportGestures(getActivity(), uri);
                        }
                    }
                    break;
                case IMPORT:
                    final Uri uri = data.getData();
                    if (uri != null) {
                        GestureUtils.importGestures(getActivity(), uri, adapter.getModels());
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
