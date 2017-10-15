package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;

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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author F43nd1r
 * @since 11.10.2017
 */

public class FormatTask3 extends FormatTask {
    public FormatTask3(ScriptManager scriptManager, Context context, ListManager listManager) {
        super(scriptManager, context, listManager);
    }

    @Override
    String beautify(String script) {
        CompilerEnvirons compilerEnvirons = new CompilerEnvirons();
        compilerEnvirons.setRecordingLocalJsDocComments(true);
        compilerEnvirons.setAllowSharpComments(true);
        compilerEnvirons.setRecordingComments(true);
        Parser parser = new Parser(compilerEnvirons);
        AstRoot astRoot = parser.parse(script + "\n", null, 1);
        AstNode root = astRoot;
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
        return root.toSource(0);
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

    private static class CloseNotes {
        AstNode prevLines;
        int prevLinesDist = Integer.MAX_VALUE;
        AstNode sameLine;
        int sameLineDist = Integer.MAX_VALUE;
        AstNode nextLines;
        int nextLinesDist = Integer.MAX_VALUE;
    }

    private static String toSource(AstNode node, Comment comment, Position position, int depth) {
        if(comment.getValue().startsWith("//onClickFunction has to have two arguments. first is group position, second is child position")){
            comment.getValue();
        }
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

        public CommentNodeWrapper(AstNode node, Comment comment, Position position) {
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

        public CommentObjectLiteralWrapper(ObjectLiteral node, Comment comment, Position position) {
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

        public CommentSwitchCaseWrapper(SwitchCase node, Comment comment, Position position) {
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
}
