package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;

import com.faendir.lightning_launcher.scriptlib.ScriptManager;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lukas on 22.01.2016.
 * Will replace FormatTask in the future, not ready yet.
 */
class FormatTask2 extends FormatTask {
    private static final String ONE_LINE_COMMENT = "//";
    private static final char LINE_BREAK_CHAR = '\n';
    private static final String LINE_BREAK = String.valueOf(LINE_BREAK_CHAR);
    private static final String MULTI_LINE_COMMENT_START = "/*";
    private static final String MULTI_LINE_COMMENT_END = "*/";
    private static final String QUOTATION_MARK = "\"";
    private static final String APOSTROPHE = "'";
    private static final char DEFAULT_CHAR = '\u0000';
    private static final String CASE = "case";
    private static final String RETURN = "return";
    private static final String TAB = "\t";
    private static final String SPACE = " ";
    private static final String CURLY_BRACKET_OPEN = "{";
    private static final String CURLY_BRACKET_CLOSE = "}";
    private static final String SWITCH = "switch";

    private static final Map<String, String> START_END_MAP = new HashMap<>();

    static {
        START_END_MAP.put(ONE_LINE_COMMENT, LINE_BREAK);
        START_END_MAP.put(MULTI_LINE_COMMENT_START, MULTI_LINE_COMMENT_END);
        START_END_MAP.put(QUOTATION_MARK, QUOTATION_MARK);
        START_END_MAP.put(APOSTROPHE, APOSTROPHE);
    }

    public FormatTask2(ScriptManager scriptManager, Context context, ListManager listManager) {
        super(scriptManager, context, listManager);
    }

    @Override
    String beautify(String script) {
        StringBuilder builder = new StringBuilder();
        boolean noCode = false;
        int indentLevel = 0;
        Deque<Integer> switchLevel = new ArrayDeque<>();
        String end = "";
        for (int i = 0; i < script.length(); i++) {
            if (isCancelled()) return null;
            publishProgress(new Progress(null, i, null));
            int length = builder.length();
            String current = String.valueOf(script.charAt(i));
            char last = length > 0 ? builder.charAt(length - 1) : DEFAULT_CHAR;
            char next = i < script.length() - 1 ? script.charAt(i + 1) : DEFAULT_CHAR;
            String lastAndCurrent = last + current;
            if (last == LINE_BREAK_CHAR) {
                indent(builder, indentLevel);
            }
            if (START_END_MAP.keySet().contains(current)) {
                noCode = true;
                end = START_END_MAP.get(current);
            } else if (START_END_MAP.keySet().contains(lastAndCurrent)) {
                noCode = true;
                end = START_END_MAP.get(lastAndCurrent);
            } else if (noCode) {
                if (end.equals(current) || end.equals(lastAndCurrent)) {
                    noCode = false;
                }
            } else if (TAB.equals(current)) {
                continue;
            } else if (SPACE.equals(current)) {
                if ((!Character.isLetter(last) || !Character.isLetter(next)) && !endsWith(builder, CASE) && !endsWith(builder, RETURN)) {
                    continue;
                }
            } else if (CURLY_BRACKET_OPEN.equals(current)) {
                indentLevel++;
                if (lastLineContains(builder, SWITCH)) {
                    switchLevel.addFirst(indentLevel);
                }
            } else if (CURLY_BRACKET_CLOSE.equals(current)) {
                indentLevel--;
                if (switchLevel.size() != 0 && i <= switchLevel.peekFirst() && switchLevel.peekFirst() > 0) {
                    switchLevel.removeFirst();
                    indentLevel--;
                }
            } else if (endsWith(builder, CASE)) {
                indentLevel = switchLevel.peekFirst() + 1;
            }
            builder.append(current);


        }
        return builder.toString();
    }

    private boolean endsWith(StringBuilder builder, String string) {
        int bLength = builder.length();
        int sLength = string.length();
        if (bLength >= sLength) {
            for (int i = sLength - 1; i >= 0; i--) {
                if (builder.charAt(bLength - (sLength - i)) != string.charAt(i)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean lastLineContains(StringBuilder builder, String string) {
        int index = builder.lastIndexOf(LINE_BREAK) + 1;
        String line = builder.substring(index);
        for (Map.Entry<String, String> entry : START_END_MAP.entrySet()) {
            while (line.contains(entry.getKey())) {
                int i1 = line.indexOf(entry.getKey());
                int i2 = line.indexOf(entry.getValue(), i1) + entry.getValue().length() + 1;
                if (i2 == -1) continue;
                if (i2 > line.length()) i2 = line.length();
                line = line.substring(0, i1) + line.substring(i2, line.length());
            }
        }
        return line.contains(string);
    }

    private void indent(StringBuilder builder, int level) {
        for (int i = 0; i < level; i++) {
            builder.append("  ");
        }
    }
}
