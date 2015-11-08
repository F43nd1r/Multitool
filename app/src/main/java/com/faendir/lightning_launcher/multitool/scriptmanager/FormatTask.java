package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.app.lukas.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.multitool.R;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Created by Lukas on 29.08.2015.
 * Formats the given scripts
 */
class FormatTask extends AsyncTask<List<ScriptItem>, FormatTask.Progress, Void> {

    private static final String SWITCH = "switch(";
    private static final String CASE = "case";
    private static final String RETURN = "return";
    private static final String DEFAULT = "default";

    private final Context context;

    public FormatTask(Context context) {
        super();
        this.context = context;
    }

    private ProgressDialog dialog;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setMessage(context.getString(R.string.message_pleaseWait));
        dialog.setTitle(context.getString(R.string.title_format));
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, context.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancel(true);
            }
        });
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
        Progress progress = values[0];
        if (progress.hasTitle()) dialog.setTitle(progress.getTitle());
        if (progress.hasMax()) dialog.setMax(progress.getMax());
        if (progress.hasProgress()) dialog.setProgress(progress.getProgress());
    }

    @SafeVarargs
    @Override
    protected final Void doInBackground(List<ScriptItem>... params) {
        for (ScriptItem item : params[0]) {
            if (item instanceof Script) {
                Script script = (Script) item;
                StringBuilder builder = new StringBuilder();
                int i = 0; //indentionlevel
                boolean noCode = false; //detectionhelper,true if in comment,string etc.
                String starter = ""; //detectionhelper,contains the last noCode block starter
                boolean newLine = true; //true if in a new line
                Deque<Integer> switchLevel = new ArrayDeque<>(); //detectionhelper,needed for switch commands
                String t = script.getCode();
                publishProgress(new Progress(t.length(), 0, script.getName()));
                for (int x = 0; x < t.length(); x++) {
                    if (isCancelled()) return null;
                    publishProgress(new Progress(null, x, null));
                    //don't copy spaces and tabs, if not in a noCode block
                    int length = builder.length();
                    char last = length == 0 ? ' ' : builder.charAt(length - 1);
                    char next = x == t.length() - 1 ? ' ' : t.charAt(x + 1);
                    char current = t.charAt(x);
                    String currentAndNext = new String(new char[]{current, next});
                    if (noCode || ((current != ' ' || isLetter(last) && isLetter(next) || endsWithCaseOrReturn(builder)) && (current != '\t'))) {
                        //detect whether the char is negated by a backslash or not
                        boolean backslashed = false;
                        for (int b = length - 1; b > 0 && builder.charAt(b) == '\\'; b--) {
                            backslashed = !backslashed;
                        }

                        //detect start of a noCode block
                        if ((current == '"' || current == '\'') && !noCode) {
                            noCode = true;
                            starter = String.valueOf(current);
                            if (last == '\n') {
                                indentLine(switchLevel, x, t, i, builder);
                                newLine = false;
                            }
                        } else if (!noCode && current == '/' && (next == '/' || next == '*')) {
                            noCode = true;
                            starter = currentAndNext;
                            if (last == '\n') {
                                indentLine(switchLevel, x, t, i, builder);
                                newLine = false;
                            }
                        }
                        //detect end of a noCode block
                        else if (noCode && !backslashed && (starter.equals(String.valueOf(current)) || (starter.equals("/*") && currentAndNext.equals("*/")) || (starter.equals("//") && current == '\n'))) {
                            noCode = false;
                        }

                        //handle keychars
                        if (!noCode) {
                            if (current == '\n') { //line end
                                newLine = true;
                            } else {
                                if (current == '}') { //function block end
                                    i--;
                                    if (switchLevel.size() != 0 && i <= switchLevel.peekFirst() && switchLevel.peekFirst() > 0) { //special handling when in switch command
                                        switchLevel.removeFirst();
                                        i--;
                                    }
                                }
                                if (newLine) {
                                    //indent the next line
                                    indentLine(switchLevel, x, t, i, builder);
                                    newLine = false;
                                }
                                if (current == '{') i++;//function block start
                                if (x < t.length() - SWITCH.length() && SWITCH.equals(t.substring(x, x + SWITCH.length()))) {
                                    switchLevel.addFirst(++i);//start of switch command
                                }
                            }
                        }
                        builder.append(current); //concat the char
                    }
                }
                while (builder.charAt(builder.length() - 1) == '\n') {
                    builder.deleteCharAt(builder.length() - 1); //remove unnecessary line brakes at the end of the script
                }
                script.setCode(builder.toString()); //set the text to the script
                Transfer transfer = new Transfer(Transfer.SET_CODE);
                transfer.script = script;
                ScriptManager.runScript(context, PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_id), -1), ScriptUtils.GSON.toJson(transfer), true);
            }
        }
        return null;
    }

    private boolean isLetter(char c) {
        return Character.toLowerCase(c) != Character.toUpperCase(c);
    }

    private boolean endsWithCaseOrReturn(StringBuilder builder) {
        int length = builder.length();
        for (int i = CASE.length()-1; i>=0;i++){
            if(builder.charAt(length - (CASE.length()-i)) != CASE.charAt(i))return false;
        }
        for (int i = RETURN.length()-1; i>=0;i++){
            if(builder.charAt(length - (RETURN.length()-i)) != RETURN.charAt(i))return false;
        }
        return true;
    }

    private void indentLine(Deque<Integer> switchLevel, int x, String t, int i, StringBuilder builder) {
        //creates tabs at the beginning of a line
        for (int b = (switchLevel.size() != 0 && ((x < t.length() - CASE.length() && CASE.equals(t.substring(x, x + CASE.length()))) || (x < t.length() - DEFAULT.length() && DEFAULT.equals(t.substring(x, x + DEFAULT.length())))) ? 1 : 0); b < i; b++) {
            builder.append("  ");
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dialog.dismiss();
        Toast.makeText(context, R.string.message_done, Toast.LENGTH_SHORT).show();
    }

    static class Progress {

        private final String title;
        private final Integer max;
        private final Integer progress;

        public Progress(@Nullable Integer max, @Nullable Integer progress, @Nullable String title) {
            this.max = max;
            this.progress = progress;
            this.title = title;
        }

        public boolean hasTitle() {
            return title != null;
        }

        public boolean hasMax() {
            return max != null;
        }

        public boolean hasProgress() {
            return progress != null;
        }

        public String getTitle() {
            return title;
        }

        public Integer getProgress() {
            return progress;
        }

        public Integer getMax() {
            return max;
        }


    }
}
