package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.Toast;

import com.app.lukas.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.multitool.Constants;
import com.faendir.lightning_launcher.multitool.R;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Lukas on 29.08.2015.
 * Backend for tasks
 */
final class ScriptUtils {
    private ScriptUtils() {
    }

    public static final Gson GSON = new Gson();

    private static final String FLAGS = "//Flags ";
    private static final String APP = "app ";
    private static final String ITEM = "item ";
    private static final String CUSTOM = "custom ";
    private static final String NAME = "Name:  ";


    public static void searchDialog(final Context context, final List<ScriptGroup> items) {
        final EditText editText = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_search)
                .setView(editText)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        search(context, items, editText.getText().toString());
                    }
                })
                .show();
    }

    private static void search(Context context, List<ScriptGroup> items, String regex) {
        StringBuilder builder = new StringBuilder(context.getString(R.string.text_matches_lines));
        Pattern pattern = Pattern.compile(regex);
        for (ScriptGroup group : items) {
            for (Script script : group) {
                boolean isFirst = true;
                String[] lines = script.getCode().split("\n");
                for (int i = 0; i < lines.length; i++) {
                    if (pattern.matcher(lines[i]).find()) {
                        if (isFirst) {
                            builder.append(script.getName()).append(": ");
                            isFirst = false;
                        } else {
                            builder.append(", ");
                        }
                        builder.append(i + 1);
                    }
                }
                if (!isFirst) builder.append("\n");
            }
        }
        new AlertDialog.Builder(context)
                .setMessage(builder.toString().trim())
                .setTitle(R.string.title_matches)
                .setNeutralButton(R.string.button_ok, null)
                .show();
    }

    public static void createGroupDialog(Context context, final List<ScriptGroup> items, final ScriptListAdapter adapter) {
        final EditText text = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_create)
                .setView(text)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createGroup(items, adapter, text.getText().toString());
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private static void createGroup(List<ScriptGroup> items, ScriptListAdapter adapter, String name) {
        items.add(new ScriptGroup(name, true));
        adapter.notifyDataSetChanged();
    }

    public static void renameDialog(final Context context, final ScriptListAdapter adapter, final ScriptItem item) {
        final EditText text = new EditText(context);
        text.setText(item.getName());
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.title_rename) + (item instanceof ScriptGroup ? context.getString(R.string.text_group) : context.getString(R.string.text_script)))
                .setView(text)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        renameItem(context, adapter, item, text.getText().toString());
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private static void renameItem(Context context, ScriptListAdapter adapter, ScriptItem item, String name) {
        item.setName(name);
        if (item instanceof Script) {
            Transfer transfer = new Transfer(Transfer.RENAME);
            transfer.script = (Script) item;
            ScriptManager.runScript(context, PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_id), -1), GSON.toJson(transfer));
        }
        adapter.deselectAll();
        adapter.notifyDataSetChanged();
    }

    public static void format(Context context, ScriptListAdapter adapter, final List<ScriptItem> selectedItems) {
        //noinspection unchecked
        new FormatTask(context).execute(selectedItems);
        adapter.deselectAll();
        adapter.notifyDataSetChanged();
    }

    public static void backup(Context context, ScriptListAdapter adapter, List<ScriptItem> selectedItems) {
        File dir = new File(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_directory), SettingsActivity.DEFAULT_BACKUP_PATH));
        dir.mkdirs();
        int success = 0;
        for (ScriptItem item : selectedItems) {
            if (item instanceof Script) {
                Script script = (Script) item;
                File file = new File(dir, script.getId() + "_" + script.getName().replace("[,\\./\\:*?\"<>\\|]", "_"));
                try {
                    FileWriter writer = null;
                    try {
                        file.createNewFile();
                        String prefix = FLAGS;
                        if ((script.getFlags() >> 1 & 1) == 1) prefix += APP;
                        if ((script.getFlags() >> 2 & 1) == 1) prefix += ITEM;
                        if ((script.getFlags() >> 3 & 1) == 1) prefix += CUSTOM;
                        prefix += " "+NAME + script.getName() + "\n";
                        writer = new FileWriter(file);
                        writer.write(prefix + script.getCode());
                        writer.flush();
                        success++;
                    } finally {
                        if (writer != null) writer.close();
                    }
                } catch (IOException e) {
                    throw new FileManager.FatalFileException(e);
                }
            }
        }
        String text = "";
        if (success > 0) {
            text += success + context.getString(R.string.text_backupSuccessful);
            if (success < selectedItems.size()) {
                text += "\n";
            }
        }
        if (success < selectedItems.size()) {
            text += context.getString(R.string.text_backupFailed) + (selectedItems.size() - success) + context.getString(R.string.text_scripts);
        }
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        adapter.deselectAll();
        adapter.notifyDataSetChanged();
    }

    public static void editScript(Context context, ScriptListAdapter adapter, Script script) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("net.pierrox.lightning_launcher_extreme", "net.pierrox.lightning_launcher.activities.ScriptEditor");
        intent.putExtra("i", script.getId());
        ScriptManager.sendIntentToLauncher(context, intent);
        adapter.deselectAll();
        adapter.notifyDataSetChanged();
    }

    public static void moveDialog(Context context, final List<ScriptGroup> items, final ScriptListAdapter adapter, final List<ScriptItem> selectedItems) {
        ArrayList<String> names = new ArrayList<>();
        for (ScriptGroup group : items) {
            names.add(group.getName());
        }
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_selectGroup)
                .setSingleChoiceItems(names.toArray(new String[names.size()]), -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        move(items, adapter, selectedItems, items.get(which));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private static void move(final List<ScriptGroup> items, ScriptListAdapter adapter, List<ScriptItem> selectedItems, ScriptGroup moveTo) {
        for (ScriptItem item : selectedItems) {
            if (item instanceof Script) {
                loop:
                for (ScriptGroup group : items) {
                    for (Script script : group) {
                        if (script.equals(item)) {
                            moveTo.add(script);
                            group.remove(script);
                            break loop;
                        }
                    }
                }
            }
        }
        adapter.deselectAll();
        adapter.notifyDataSetChanged();
    }

    public static void deleteDialog(final Context context, final List<ScriptGroup> items, final ScriptListAdapter adapter, final List<ScriptItem> selectedItems) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_delete)
                .setMessage(context.getString(R.string.message_deletePart1) + selectedItems.size() + context.getString(R.string.message_deletePart2))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItems(context, items, adapter, selectedItems);
                    }
                }).setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private static void deleteItems(Context context, final List<ScriptGroup> items, final ScriptListAdapter adapter, List<ScriptItem> selectedItems) {
        for (ScriptItem item : selectedItems) {
            loop:
            for (ScriptGroup group : items) {
                if (group.equals(item)) {
                    if (prepareGroupForDelete(items, group)) {
                        items.remove(group);
                    }
                    break;
                } else {
                    for (Script script : group) {
                        if (script.equals(item)) {
                            deleteScript(context, script);
                            group.remove(script);
                            break loop;
                        }
                    }
                }
            }
        }
        adapter.deselectAll();
        adapter.notifyDataSetChanged();
    }

    private static void deleteScript(Context context, Script delete) {
        Transfer transfer = new Transfer(Transfer.DELETE);
        transfer.script = delete;
        ScriptManager.runScript(context, PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_id), -1), GSON.toJson(transfer));
    }

    private static boolean prepareGroupForDelete(List<ScriptGroup> items, ScriptGroup delete) {
        if (!delete.allowsDelete()) return false;
        ScriptGroup def = null;
        for (ScriptGroup s : items) {
            if (!s.allowsDelete()) {
                def = s;
            }
        }
        assert def != null;
        for (Script item : delete) {
            def.add(item);
        }
        return true;
    }

    public static void restoreFromFile(final Context context, final List<ScriptGroup> items, Uri uri) {
        File file = new File(uri.getEncodedPath());
        if (file.exists() && file.canRead()) {
            try {
                FileReader reader = null;
                try {
                    reader = new FileReader(file);
                    char[] buffer = new char[(int) file.length()];
                    reader.read(buffer);
                    restoreDialog(context,items, new String(buffer), file.getName());

                } catch (IOException e) {
                } finally {
                    if (reader != null) reader.close();
                }
            } catch (IOException e) {
                throw new FileManager.FatalFileException(e);
            }
        }
    }

    private static void restoreDialog(final Context context, final List<ScriptGroup> items, String s, String filename) {
        int endOfFirstLine = s.indexOf('\n');
        if (endOfFirstLine == -1) endOfFirstLine = s.length();
        String l = s.substring(0, endOfFirstLine);
        int flags = 0;
        if (l.contains(FLAGS)) { //check if file contains flag settings
            if (l.contains(APP)) flags += Constants.FLAG_APP_MENU;
            if (l.contains(ITEM)) flags += Constants.FLAG_ITEM_MENU;
            if (l.contains(CUSTOM)) flags += Constants.FLAG_CUSTOM_MENU;
            s = s.substring(endOfFirstLine + 1);
        }
        String nameFromFile = "";
        if (l.contains(NAME)) {
            nameFromFile = l.substring(l.indexOf(NAME) + NAME.length()).trim();
        }
        if (nameFromFile.equals("")) {
            //remove file extension
            nameFromFile = filename;
            int index = nameFromFile.lastIndexOf('.');
            if (index != -1) nameFromFile = nameFromFile.substring(0, index);
        }
        //ask for imported scripts name
        final EditText editText = new EditText(context);
        editText.setText(nameFromFile);
        final String finalS = s;
        final int finalFlags = flags;
        new AlertDialog.Builder(context)
                .setView(editText)
                .setTitle(R.string.title_chooseName)
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prepareRestore(context, items, finalS, editText.getText().toString(), finalFlags);
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private static void prepareRestore(final Context context, final List<ScriptGroup> items, String code, String name, int flags) {
        final Script script = new Script();
        script.setName(name);
        script.setFlags(flags);
        script.setCode(code);
        for (ScriptGroup group : items) {
            for (Script s : group) {
                if (s.equals(script)) {
                    new AlertDialog.Builder(context)
                            .setMessage(R.string.message_overwrite)
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    restore(context, script);
                                }
                            })
                            .setNegativeButton(R.string.button_cancel, null)
                            .show();
                    return;
                }
            }
        }
        restore(context, script);
    }

    private static void restore(final Context context,Script script) {
        Transfer transfer = new Transfer(Transfer.RESTORE);
        transfer.script = script;
        ScriptManager.runScript(context, PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_id), -1), GSON.toJson(transfer));
    }


}
