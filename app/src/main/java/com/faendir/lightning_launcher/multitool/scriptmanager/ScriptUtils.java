package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.Loader;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.Model;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.executor.DirectScriptExecutor;

import org.acra.util.StreamReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
        new AlertDialog.Builder(context).setTitle(R.string.title_search).setView(editText).setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_ok, (dialog, ignore) -> search(context, listManager.getItems(), editText.getText().toString())).show();
    }

    private static void search(Context context, List<Model> items, String regex) {
        final StringBuilder builder = new StringBuilder(context.getString(R.string.text_matches_lines));
        final Pattern pattern = Pattern.compile(regex);
        for (Model item : items) {
            if (item instanceof Script) {
                Script script = (Script) item;
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
        new AlertDialog.Builder(context).setMessage(builder.toString().trim()).setTitle(R.string.title_matches).setNeutralButton(R.string.button_ok, null).show();
    }

    public static void renameDialog(final ScriptManager scriptManager, final Context context, final ListManager listManager, final Model item) {
        final EditText text = new EditText(context);
        text.setText(item.getName());
        new AlertDialog.Builder(context).setTitle(
                context.getString(R.string.title_rename, (item instanceof Folder ? context.getString(R.string.text_folder) : context.getString(R.string.text_script))))
                .setView(text).setPositiveButton(R.string.button_ok, (dialog, ignore) -> renameItem(scriptManager, listManager, item, text.getText().toString()))
                .setNegativeButton(R.string.button_cancel, null).show();
    }

    private static void renameItem(final ScriptManager scriptManager, final ListManager listManager, Model item, String name) {
        if (item instanceof Script) {
            ((Script) item).setName(name);
            final Transfer transfer = new Transfer(Transfer.RENAME);
            transfer.script = (Script) item;
            scriptManager.getAsyncExecutorService()
                    .add(new DirectScriptExecutor(R.raw.scriptmanager).putVariable("data", Utils.GSON.toJson(transfer)), result -> updateFrom(result, listManager)).start();
        } else if (item instanceof Folder) {
//TODO
        }
        listManager.deselectAll();
    }

    private static void updateFrom(@Nullable String result, ListManager listManager) {
        if (result != null) {
            listManager.updateFrom(Arrays.asList(Utils.GSON.fromJson(result, Script[].class)));
        }
    }

    public static void format(ScriptManager scriptManager, Context context, ListManager listManager, final List<Model> selectedItems) {
        new FormatTask3(scriptManager, context, listManager).execute(selectedItems.toArray(new Model[selectedItems.size()]));
        listManager.deselectAll();
    }

    public static void backup(final Context context, final ListManager listManager, Script script, Uri uri) {
        String prefix = FLAGS;
        if ((script.getFlags() >> 1 & 1) == 1) prefix += APP;
        if ((script.getFlags() >> 2 & 1) == 1) prefix += ITEM;
        if ((script.getFlags() >> 3 & 1) == 1) prefix += CUSTOM;
        prefix += " " + NAME + script.getName() + "\n";
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream == null) throw new FileNotFoundException();
            try (Writer writer = new OutputStreamWriter(outputStream)) {
                writer.write(prefix + script.getCode());
                writer.flush();
                Toast.makeText(context, R.string.text_backupSuccessful, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(context, R.string.text_backupFailed, Toast.LENGTH_SHORT).show();
        } finally {
            listManager.deselectAll();
        }
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
                .add(new DirectScriptExecutor(R.raw.scriptmanager).putVariable("data", Utils.GSON.toJson(transfer)), result -> updateFrom(result, listManager)).start();
    }

    public static void restoreFromFile(ScriptManager scriptManager, Context context, ListManager listManager, Uri uri) {
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in != null) {
                restoreDialog(scriptManager, context, listManager, new StreamReader(in).read(), uri.getLastPathSegment());
                return;
            }
        } catch (IOException ignored) {
        }
        Toast.makeText(context, context.getString(R.string.toast_failedLoad) + uri.getPath(), Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(context).setView(editText).setTitle(R.string.title_chooseName).setPositiveButton(R.string.button_ok,
                (dialog, ignore) -> prepareRestore(scriptManager, context, listManager, finalS, editText.getText().toString(), finalFlags))
                .setNegativeButton(R.string.button_cancel, null).show();
    }

    private static void prepareRestore(final ScriptManager scriptManager, final Context context, final ListManager listManager, String code, String name, int flags) {
        final Script script = new Script(name, 0, code, flags, "/");
        if (listManager.exists(script)) {
            new AlertDialog.Builder(context).setMessage(R.string.message_overwrite)
                    .setPositiveButton(R.string.button_ok, (dialog, ignore) -> restore(scriptManager, listManager, script)).setNegativeButton(R.string.button_cancel, null)
                    .show();
        } else {
            restore(scriptManager, listManager, script);
        }

    }

    private static void restore(final ScriptManager scriptManager, final ListManager listManager, Script script) {
        final Transfer transfer = new Transfer(Transfer.RESTORE);
        transfer.script = script;
        scriptManager.getAsyncExecutorService()
                .add(new DirectScriptExecutor(R.raw.scriptmanager).putVariable("data", Utils.GSON.toJson(transfer)), result -> updateFrom(result, listManager)).start();
    }

    public static void toggleDisable(final ScriptManager scriptManager, final ListManager listManager, Script item) {
        final Transfer transfer = new Transfer(Transfer.TOGGLE_DISABLE);
        transfer.script = item;
        scriptManager.getAsyncExecutorService()
                .add(new DirectScriptExecutor(R.raw.scriptmanager).putVariable("data", Utils.GSON.toJson(transfer)), result -> updateFrom(result, listManager)).start();
        item.setFlags(item.getFlags() ^ Loader.FLAG_DISABLED);
        listManager.deselectAll();
    }


}
