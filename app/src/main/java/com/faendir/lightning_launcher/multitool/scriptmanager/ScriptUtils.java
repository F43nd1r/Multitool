package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.Loader;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.settings.PrefsFragment;
import com.faendir.lightning_launcher.multitool.util.FileManager;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.scriptlib.PermissionActivity;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.executor.DirectScriptExecutor;
import com.faendir.omniadapter.model.DeepObservableList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Lukas on 29.08.2015.
 * Backend for tasks
 */
final class ScriptUtils {
    private ScriptUtils() {
    }

    private static final String FLAGS = "//Flags ";
    private static final String APP = "app ";
    private static final String ITEM = "item ";
    private static final String CUSTOM = "custom ";
    private static final String NAME = "Name:  ";


    public static void searchDialog(final Context context, final ListManager listManager) {
        final EditText editText = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle(R.string.title_search)
                .setView(editText)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok, (dialog, ignore) -> search(context, listManager.getItems(), editText.getText().toString()))
                .show();
    }

    private static void search(Context context, DeepObservableList<ScriptItem> items, String regex) {
        final StringBuilder builder = new StringBuilder(context.getString(R.string.text_matches_lines));
        final Pattern pattern = Pattern.compile(regex);
        items.visitDeep((component, level) -> {
            if (component instanceof Script) {
                Script script = (Script) component;
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
        }, false);
        new AlertDialog.Builder(context)
                .setMessage(builder.toString().trim())
                .setTitle(R.string.title_matches)
                .setNeutralButton(R.string.button_ok, null)
                .show();
    }

    public static void renameDialog(final ScriptManager scriptManager, final Context context, final ListManager listManager, final ScriptItem item) {
        final EditText text = new EditText(context);
        text.setText(item.getName());
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.title_rename, (item instanceof Folder ? context.getString(R.string.text_folder) : context.getString(R.string.text_script))))
                .setView(text)
                .setPositiveButton(R.string.button_ok, (dialog, ignore) -> renameItem(scriptManager, listManager, item, text.getText().toString()))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private static void renameItem(final ScriptManager scriptManager, final ListManager listManager, ScriptItem item, String name) {
        item.setName(name);
        if (item instanceof Script) {
            final Transfer transfer = new Transfer(Transfer.RENAME);
            transfer.script = (Script) item;
            scriptManager.getAsyncExecutorService()
                    .add(new DirectScriptExecutor(R.raw.scriptmanager).putVariable("data", Utils.GSON.toJson(transfer)), result -> updateFrom(result, listManager))
                    .start();
        } else if (item instanceof Folder) {
//TODO
        }
        listManager.changed(item);
        listManager.deselectAll();
    }

    private static void updateFrom(@Nullable String result, ListManager listManager) {
        if (result != null) {
            listManager.updateFrom(Arrays.asList(Utils.GSON.fromJson(result, Script[].class)));
        }
    }

    public static void format(ScriptManager scriptManager, Context context, ListManager listManager, final List<ScriptItem> selectedItems) {
        new FormatTask(scriptManager, context, listManager).execute(selectedItems.toArray(new ScriptItem[selectedItems.size()]));
        listManager.deselectAll();
    }

    public static void backup(final Context context, final ListManager listManager, List<ScriptItem> selectedItems) {
        final List<ScriptItem> selectedItemsFinal = new ArrayList<>(selectedItems);
        final File dir = new File(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_directory), PrefsFragment.DEFAULT_BACKUP_PATH));
        if ((!dir.mkdirs() && !dir.isDirectory()) || !dir.canWrite()) {
            PermissionActivity.checkForPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, isGranted -> {
                if (isGranted && (dir.mkdirs() || dir.isDirectory()) && dir.canWrite()) {
                    backup0(context, listManager, dir, selectedItemsFinal);
                } else {
                    Toast.makeText(context, R.string.toast_failedDirWrite, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            backup0(context, listManager, dir, selectedItemsFinal);
        }
    }

    private static void backup0(Context context, ListManager listManager, File dir, List<ScriptItem> selectedItems) {
        String text = "";
        int success = 0;
        for (ScriptItem item : selectedItems) {
            if (item instanceof Script) {
                Script script = (Script) item;
                File file = new File(dir, script.getId() + "_" + script.getName().replace("[,\\./\\:*?\"<>\\|]", "_"));
                String prefix = FLAGS;
                if ((script.getFlags() >> 1 & 1) == 1) prefix += APP;
                if ((script.getFlags() >> 2 & 1) == 1) prefix += ITEM;
                if ((script.getFlags() >> 3 & 1) == 1) prefix += CUSTOM;
                prefix += " " + NAME + script.getName() + "\n";
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(prefix + script.getCode());
                    writer.flush();
                    success++;
                } catch (IOException e) {
                    throw new FileManager.FatalFileException(e);
                }
            }
        }
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
        listManager.deselectAll();
    }

    public static void editScript(Context context, ListManager listManager, Script script) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("net.pierrox.lightning_launcher_extreme", "net.pierrox.lightning_launcher.activities.ScriptEditor");
        intent.putExtra("i", script.getId());
        context.startActivity(intent);
        listManager.deselectAll();
    }

    public static void deleteScript(final ScriptManager scriptManager, final ListManager listManager, Script delete) {
        final Transfer transfer = new Transfer(Transfer.DELETE);
        transfer.script = delete;
        scriptManager.getAsyncExecutorService()
                .add(new DirectScriptExecutor(R.raw.scriptmanager).putVariable("data", Utils.GSON.toJson(transfer)), result -> updateFrom(result, listManager))
                .start();
    }

    public static void restoreFromFile(ScriptManager scriptManager, Context context, ListManager listManager, Uri uri) {
        File file = new File(uri.getPath());
        if (file.exists() && file.canRead()) {
            try (FileReader reader = new FileReader(file)) {
                char[] buffer = new char[(int) file.length()];
                reader.read(buffer);
                restoreDialog(scriptManager, context, listManager, new String(buffer), file.getName());
            } catch (IOException e) {
                throw new FileManager.FatalFileException(e);
            }
        } else {
            Toast.makeText(context, context.getString(R.string.toast_failedLoad) + uri.getPath(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void restoreDialog(final ScriptManager scriptManager, final Context context, final ListManager listManager, String s, String filename) {
        int endOfFirstLine = s.indexOf('\n');
        if (endOfFirstLine == -1) endOfFirstLine = s.length();
        String l = s.substring(0, endOfFirstLine);
        int flags = 0;
        if (l.contains(FLAGS)) { //check if file contains flag settings
            if (l.contains(APP)) flags += Loader.FLAG_APP_MENU;
            if (l.contains(ITEM)) flags += Loader.FLAG_ITEM_MENU;
            if (l.contains(CUSTOM)) flags += Loader.FLAG_CUSTOM_MENU;
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
                .setPositiveButton(R.string.button_ok, (dialog, ignore) -> prepareRestore(scriptManager, context, listManager, finalS, editText.getText().toString(), finalFlags))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private static void prepareRestore(final ScriptManager scriptManager, final Context context, final ListManager listManager, String code, String name, int flags) {
        final Script script = new Script();
        script.setName(name);
        script.setFlags(flags);
        script.setCode(code);
        if (listManager.exists(script)) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.message_overwrite)
                    .setPositiveButton(R.string.button_ok, (dialog, ignore) -> restore(scriptManager, listManager, script))
                    .setNegativeButton(R.string.button_cancel, null)
                    .show();
        } else {
            restore(scriptManager, listManager, script);
        }

    }

    private static void restore(final ScriptManager scriptManager, final ListManager listManager, Script script) {
        final Transfer transfer = new Transfer(Transfer.RESTORE);
        transfer.script = script;
        scriptManager.getAsyncExecutorService()
                .add(new DirectScriptExecutor(R.raw.scriptmanager).putVariable("data", Utils.GSON.toJson(transfer)), result -> updateFrom(result, listManager))
                .start();
    }


}
