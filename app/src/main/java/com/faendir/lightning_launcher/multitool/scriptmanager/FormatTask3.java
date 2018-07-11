package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.Model;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Comment;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author F43nd1r
 * @since 11.10.2017
 */

public class FormatTask3 extends AsyncTask<Model, FormatTask3.Progress, Void> {
    private final ScriptManager scriptManager;
    private final WeakReference<Context> context;
    private final ListManager listManager;
    private ProgressDialog dialog;

    FormatTask3(ScriptManager scriptManager, Context context, ListManager listManager) {
        super();
        this.scriptManager = scriptManager;
        this.context = new WeakReference<>(context);
        this.listManager = listManager;
    }

    private String beautify(String script) {
        try {
            CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
            compilerEnvirons.setRecordingLocalJsDocComments(true);
            compilerEnvirons.setAllowSharpComments(true);
            compilerEnvirons.setRecordingComments(true);
            Parser parser = new Parser(compilerEnvirons);
            AstRoot astRoot = parser.parse(script + "\n", null, 1);
            AstNode root = astRoot;
            if (astRoot.getComments() != null) {
                for (Comment comment : astRoot.getComments()) {
                    final CloseNotes closeNotes = new CloseNotes();
                    root.visit(node -> {
                        if (comment.getCommentType() == Token.CommentType.BLOCK_COMMENT ||
                                !(node.getParent() instanceof SwitchCase && matches(node, ((SwitchCase) node.getParent()).getExpression())
                                        || node.getParent() instanceof IfStatement && matches(node, ((IfStatement) node.getParent()).getCondition()))) {
                            int dist1 = Math.abs(comment.getAbsolutePosition() - node.getAbsolutePosition());
                            int dist2 = Math.abs(comment.getAbsolutePosition() + comment.getLength() - node.getAbsolutePosition());
                            int dist3 = Math.abs(comment.getAbsolutePosition() - node.getAbsolutePosition() - node.getLength());
                            int dist4 = Math.abs(comment.getAbsolutePosition() + comment.getLength() - node.getAbsolutePosition() - node.getLength());
                            int dist = Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4));
                            if (comment.getLineno() == node.getLineno()) {
                                if (dist < closeNotes.sameLineDist) {
                                    closeNotes.sameLine = node;
                                    closeNotes.sameLineDist = dist;
                                }
                            } else if (comment.getLineno() > node.getLineno()) {
                                if (dist < closeNotes.prevLinesDist) {
                                    closeNotes.prevLines = node;
                                    closeNotes.prevLinesDist = dist;
                                }
                            } else {
                                if (dist < closeNotes.nextLinesDist) {
                                    closeNotes.nextLines = node;
                                    closeNotes.nextLinesDist = dist;
                                }
                            }
                            return true;
                        }
                        return false;
                    });
                    if (closeNotes.sameLine != null) {
                        boolean in = comment.getLength() < closeNotes.sameLine.getLength() && comment.getAbsolutePosition() > closeNotes.sameLine.getAbsolutePosition()
                                && comment.getAbsolutePosition() + comment.getLength() < closeNotes.sameLine.getAbsolutePosition() + closeNotes.sameLine.getLength();
                        boolean before = Math.abs(comment.getAbsolutePosition() + comment.getLength() - closeNotes.sameLine.getAbsolutePosition())
                                < Math.abs(comment.getAbsolutePosition() - closeNotes.sameLine.getAbsolutePosition() - closeNotes.sameLine.getLength());
                        root = replaceWithWrapper(closeNotes.sameLine, comment, in ? Position.IN : before ? Position.BEFORE : Position.AFTER_SAME_LINE, root);
                    } else if (closeNotes.nextLinesDist > closeNotes.prevLinesDist) {
                        root = replaceWithWrapper(closeNotes.prevLines, comment, Position.AFTER_NEXT_LINE, root);
                    } else if (closeNotes.nextLines != null) {
                        root = replaceWithWrapper(closeNotes.nextLines, comment, Position.BEFORE, root);
                    } else {
                        root = replaceWithWrapper(root, comment, Position.AFTER_SAME_LINE, root);
                    }
                }
            }
            return root.toSource(0);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private AstNode replaceWithWrapper(AstNode node, Comment comment, Position position, AstNode root) {
        if (node.getParent() == null) {
            return new CommentNodeWrapper(node, comment, position);
        } else if (node instanceof ObjectLiteral && node.getParent() instanceof NewExpression) {
            ((NewExpression) node.getParent()).setInitializer(new CommentObjectLiteralWrapper((ObjectLiteral) node, comment, position));
        } else if (node instanceof SwitchCase && node.getParent() instanceof SwitchStatement) {
            List<SwitchCase> cases = new ArrayList<>(((SwitchStatement) node.getParent()).getCases());
            int pos = cases.indexOf(node);
            cases.remove(node);
            cases.add(pos, new CommentSwitchCaseWrapper((SwitchCase) node, comment, position));
            ((SwitchStatement) node.getParent()).setCases(cases);
        } else {
            for (Field field : FieldUtils.getAllFields(node.getParent().getClass())) {
                if (!"first".equals(field.getName()) && !"last".equals(field.getName())) {
                    if (AstNode.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        try {
                            AstNode value = (AstNode) field.get(node.getParent());
                            if (matches(node, value)) {
                                field.set(node.getParent(), new CommentNodeWrapper(value, comment, position));
                                return root;
                            }
                        } catch (IllegalAccessException ignored) {
                        }
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        try {
                            List list = (List) field.get(node.getParent());
                            if (list != null) {
                                for (Object o : list) {
                                    if (o instanceof AstNode && matches(node, (Node) o)) {
                                        int pos = list.indexOf(node);
                                        list.remove(pos);
                                        //noinspection unchecked
                                        list.add(pos, new CommentNodeWrapper((AstNode) o, comment, position));
                                        return root;
                                    }
                                }
                            }
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                }
            }
            if (matches(node, node.getParent().getFirstChild())) {
                node.getParent().replaceChild(node, new CommentNodeWrapper((AstNode) node.getParent().getFirstChild(), comment, position));
            }
            Node prev = node.getParent().getFirstChild();
            while (prev != null) {
                Node n = prev.getNext();
                if (matches(node, n)) {
                    node.getParent().replaceChildAfter(prev, new CommentNodeWrapper((AstNode) n, comment, position));
                    break;
                }
                prev = n;
            }
        }
        return root;
    }

    private boolean matches(Node node, Node n) {
        if (n instanceof CommentNodeWrapper) {
            return matches(node, ((CommentNodeWrapper) n).node);
        } else if (n instanceof CommentObjectLiteralWrapper) {
            return matches(node, ((CommentObjectLiteralWrapper) n).node);
        } else {
            return node == n;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context context = this.context.get();
        if (context != null) {
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            dialog.setMessage(context.getString(R.string.message_pleaseWait));
            dialog.setTitle(context.getString(R.string.title_format));
            dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, context.getString(R.string.button_cancel), (dialog1, ignore) -> cancel(true));
            dialog.show();
        }
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
        Progress progress = values[0];
        if (dialog != null) {
            if (progress.hasTitle()) dialog.setTitle(progress.getTitle());
            if (progress.hasMax()) dialog.setMax(progress.getMax());
            if (progress.hasProgress()) dialog.setProgress(progress.getProgress());
        }
    }

    @Override
    protected Void doInBackground(Model... params) {
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Script) {
                Script script = (Script) params[i];
                publishProgress(new Progress(params.length, i, script.getName()));
                String code = beautify(script.getCode());
                if (code == null) return null;
                script.setCode(code); //set the text to the script
                Transfer transfer = new Transfer(Transfer.SET_CODE);
                transfer.script = script;
                scriptManager.getAsyncExecutorService().add(ScriptUtils.getScriptManagerExecutor(Utils.GSON.toJson(transfer)), result -> {
                    if (result != null) {
                        List<Script> scripts = Arrays.asList(Utils.GSON.fromJson(result, Script[].class));
                        listManager.updateFrom(scripts);
                    }
                }).start();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (dialog != null) {
            dialog.dismiss();
        }
        Context context = this.context.get();
        if (context != null) {
            Toast.makeText(context, R.string.message_done, Toast.LENGTH_SHORT).show();
        }
    }

    private static class CloseNotes {
        AstNode prevLines;
        int prevLinesDist = Integer.MAX_VALUE;
        AstNode sameLine;
        int sameLineDist = Integer.MAX_VALUE;
        AstNode nextLines;
        int nextLinesDist = Integer.MAX_VALUE;
    }

    private static String toSource(AstNode node, Comment comment, Position position, int depth) {
        String source = node.toSource(depth);
        switch (position) {
            case BEFORE:
                if (!source.startsWith("\n") && comment.getCommentType() != Token.CommentType.BLOCK_COMMENT) {
                    source = "\n" + source;
                }
                return "\n" + node.makeIndent(depth) + comment.getValue() + source;
            case IN:
                int i = source.indexOf('\n');
                if (i != -1) {
                    return source.substring(0, i) + " " + comment.getValue() + source.substring(i);
                }
                //fallthrough
            case AFTER_SAME_LINE:
                if (source.endsWith("\n")) {
                    return source.substring(0, source.length() - 1) + " " + comment.getValue() + "\n";
                }
                return source + " " + comment.getValue();
            case AFTER_NEXT_LINE:
                if (!source.endsWith("\n")) {
                    source += "\n";
                }
                return source + node.makeIndent(depth) + comment.getValue() + "\n";
            case ONLY_COMMENT:
                return comment.getValue();
            default:
                return source;
        }
    }

    private static class CommentNodeWrapper extends AstNode {

        private final AstNode node;
        private final Comment comment;
        private final Position position;

        CommentNodeWrapper(AstNode node, Comment comment, Position position) {
            this.node = node;
            this.comment = comment;
            this.position = position;
        }

        @Override
        public String toSource(int depth) {
            return FormatTask3.toSource(node, comment, position, depth);
        }

        @Override
        public void visit(NodeVisitor visitor) {
            node.visit(visitor);
        }

        @Override
        public int getType() {
            return node.getType();
        }
    }

    private static class CommentObjectLiteralWrapper extends ObjectLiteral {

        private final ObjectLiteral node;
        private final Comment comment;
        private final Position position;

        CommentObjectLiteralWrapper(ObjectLiteral node, Comment comment, Position position) {
            this.node = node;
            this.comment = comment;
            this.position = position;
        }

        @Override
        public String toSource(int depth) {
            return FormatTask3.toSource(node, comment, position, depth);
        }

        @Override
        public void visit(NodeVisitor visitor) {
            node.visit(visitor);
        }

        @Override
        public int getType() {
            return node.getType();
        }
    }

    private static class CommentSwitchCaseWrapper extends SwitchCase {

        private final SwitchCase node;
        private final Comment comment;
        private final Position position;

        CommentSwitchCaseWrapper(SwitchCase node, Comment comment, Position position) {
            this.node = node;
            this.comment = comment;
            this.position = position;
        }

        @Override
        public String toSource(int depth) {
            return FormatTask3.toSource(node, comment, position, depth);
        }

        @Override
        public void visit(NodeVisitor visitor) {
            node.visit(visitor);
        }

        @Override
        public int getType() {
            return node.getType();
        }
    }

    private enum Position {
        BEFORE,
        IN,
        AFTER_SAME_LINE,
        AFTER_NEXT_LINE,
        ONLY_COMMENT
    }

    static class Progress {

        private final String title;
        private final Integer max;
        private final Integer progress;

        Progress(@Nullable Integer max, @Nullable Integer progress, @Nullable String title) {
            this.max = max;
            this.progress = progress;
            this.title = title;
        }

        boolean hasTitle() {
            return title != null;
        }

        boolean hasMax() {
            return max != null;
        }

        boolean hasProgress() {
            return progress != null;
        }

        public String getTitle() {
            return title;
        }

        Integer getProgress() {
            return progress;
        }

        Integer getMax() {
            return max;
        }


    }
}
