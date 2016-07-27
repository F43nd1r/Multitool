package com.faendir.lightning_launcher.multitool.gesture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.ListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 28.01.2016.
 *
 * @author F43nd1r
 */
final class GestureUtils {
    private GestureUtils() {
    }

    static void deleteDialog(final Context context, final List<GestureInfo> selected, final ListAdapter<GestureInfo> adapter, final FileManager<GestureInfo> fileManager) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_delete)
                .setMessage(context.getString(R.string.message_deletePart1) + selected.size() + context.getString(R.string.message_deletePart2))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete(context, selected, adapter, fileManager);
                    }
                }).setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private static void delete(Context context, List<GestureInfo> selected, ListAdapter<GestureInfo> adapter, FileManager<GestureInfo> fileManager) {
        for (GestureInfo info : selected) {
            info.removeGesture(context);
            adapter.remove(info);
        }
        updateSavedGestures(adapter, fileManager);
    }

    static void edit(Activity context, GestureInfo selected, ListAdapter<GestureInfo> adapter){
        Intent intent = new Intent(context, GestureActivity.class);
        intent.putExtra(GestureActivity.GESTURE, selected);
        intent.putExtra(GestureFragment.INDEX, adapter.getPosition(selected));
        context.startActivityForResult(intent, GestureFragment.EDIT);

    }

    static void updateSavedGestures(ListAdapter<GestureInfo> adapter, FileManager<GestureInfo> fileManager) {
        ArrayList<GestureInfo> gestureInfos = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            gestureInfos.add(adapter.getItem(i));
        }
        fileManager.write(gestureInfos);
    }
}
