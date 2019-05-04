package com.faendir.lightning_launcher.multitool.scriptmanager

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.fastadapter.Model
import org.apache.commons.lang3.reflect.FieldUtils
import org.mozilla.javascript.CompilerEnvirons
import org.mozilla.javascript.Node
import org.mozilla.javascript.Parser
import org.mozilla.javascript.Token
import org.mozilla.javascript.ast.*
import java.lang.ref.WeakReference
import java.util.*

/**
 * @author F43nd1r
 * @since 11.10.2017
 */

@Suppress("DEPRECATION")
class FormatTask3 internal constructor(context: Context) : AsyncTask<Model, FormatTask3.Progress, Void>() {
    private val context: WeakReference<Context> = WeakReference(context)
    private var dialog: ProgressDialog? = null

    private fun beautify(script: String): String? {
        try {
            val compilerEnvirons = CompilerEnvirons()
            compilerEnvirons.isRecordingLocalJsDocComments = true
            compilerEnvirons.allowSharpComments = true
            compilerEnvirons.isRecordingComments = true
            val parser = Parser(compilerEnvirons)
            val astRoot = parser.parse(script + "\n", null, 1)
            var root: AstNode = astRoot
            if (astRoot.comments != null) {
                for (comment in astRoot.comments) {
                    val closeNotes = CloseNotes()
                    root.visit { node ->
                        if (comment.commentType == Token.CommentType.BLOCK_COMMENT || !(node.parent is SwitchCase && matches(node, (node.parent as SwitchCase).expression) || node.parent is IfStatement && matches(node, (node.parent as IfStatement).condition))) {
                            val dist1 = Math.abs(comment.absolutePosition - node.absolutePosition)
                            val dist2 = Math.abs(comment.absolutePosition + comment.length - node.absolutePosition)
                            val dist3 = Math.abs(comment.absolutePosition - node.absolutePosition - node.length)
                            val dist4 = Math.abs(comment.absolutePosition + comment.length - node.absolutePosition - node.length)
                            val dist = Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4))
                            if (comment.lineno == node.lineno) {
                                if (dist < closeNotes.sameLineDist) {
                                    closeNotes.sameLine = node
                                    closeNotes.sameLineDist = dist
                                }
                            } else if (comment.lineno > node.lineno) {
                                if (dist < closeNotes.prevLinesDist) {
                                    closeNotes.prevLines = node
                                    closeNotes.prevLinesDist = dist
                                }
                            } else {
                                if (dist < closeNotes.nextLinesDist) {
                                    closeNotes.nextLines = node
                                    closeNotes.nextLinesDist = dist
                                }
                            }
                            true
                        } else false
                    }
                    root = when {
                        closeNotes.sameLine != null -> {
                            val `in` = (comment.length < closeNotes.sameLine!!.length && comment.absolutePosition > closeNotes.sameLine!!.absolutePosition
                                    && comment.absolutePosition + comment.length < closeNotes.sameLine!!.absolutePosition + closeNotes.sameLine!!.length)
                            val before = Math.abs(comment.absolutePosition + comment.length - closeNotes.sameLine!!.absolutePosition) < Math.abs(comment.absolutePosition - closeNotes.sameLine!!.absolutePosition - closeNotes.sameLine!!.length)
                            replaceWithWrapper(closeNotes.sameLine!!, comment, if (`in`) Position.IN else if (before) Position.BEFORE else Position.AFTER_SAME_LINE, root)
                        }
                        closeNotes.nextLinesDist > closeNotes.prevLinesDist -> replaceWithWrapper(closeNotes.prevLines!!, comment, Position.AFTER_NEXT_LINE, root)
                        closeNotes.nextLines != null -> replaceWithWrapper(closeNotes.nextLines!!, comment, Position.BEFORE, root)
                        else -> replaceWithWrapper(root, comment, Position.AFTER_SAME_LINE, root)
                    }
                }
            }
            return root.toSource(0)
        } catch (t: Throwable) {
            t.printStackTrace()
            return null
        }

    }

    private fun replaceWithWrapper(node: AstNode, comment: Comment, position: Position, root: AstNode): AstNode {
        if (node.parent == null) {
            return CommentNodeWrapper(node, comment, position)
        } else if (node is ObjectLiteral && node.getParent() is NewExpression) {
            (node.getParent() as NewExpression).initializer = CommentObjectLiteralWrapper(node, comment, position)
        } else if (node is SwitchCase && node.getParent() is SwitchStatement) {
            val cases = ArrayList((node.getParent() as SwitchStatement).cases)
            val pos = cases.indexOf(node)
            cases.remove(node)
            cases.add(pos, CommentSwitchCaseWrapper(node, comment, position))
            (node.getParent() as SwitchStatement).cases = cases
        } else {
            for (field in FieldUtils.getAllFields(node.parent.javaClass)) {
                if ("first" != field.name && "last" != field.name) {
                    if (AstNode::class.java.isAssignableFrom(field.type)) {
                        field.isAccessible = true
                        try {
                            val value = field.get(node.parent) as AstNode
                            if (matches(node, value)) {
                                field.set(node.parent, CommentNodeWrapper(value, comment, position))
                                return root
                            }
                        } catch (ignored: IllegalAccessException) {
                        }
                    } else if (List::class.java.isAssignableFrom(field.type)) {
                        field.isAccessible = true
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val list = field.get(node.parent) as? MutableList<Any>
                            if (list != null) {
                                for (o in list) {
                                    if (o is AstNode && matches(node, o as Node)) {
                                        val pos = list.indexOf(node)
                                        list.removeAt(pos)
                                        list[pos] = CommentNodeWrapper(o, comment, position)
                                        return root
                                    }
                                }
                            }
                        } catch (ignored: IllegalAccessException) {
                        }
                    }
                }
            }
            if (matches(node, node.parent.firstChild)) {
                node.parent.replaceChild(node, CommentNodeWrapper(node.parent.firstChild as AstNode, comment, position))
            }
            var prev: Node? = node.parent.firstChild
            while (prev != null) {
                val n = prev.next
                if (matches(node, n)) {
                    node.parent.replaceChildAfter(prev, CommentNodeWrapper(n as AstNode, comment, position))
                    break
                }
                prev = n
            }
        }
        return root
    }

    private fun matches(node: Node, n: Node): Boolean {
        return when (n) {
            is CommentNodeWrapper -> matches(node, n.node)
            is CommentObjectLiteralWrapper -> matches(node, n.node)
            else -> node === n
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        val context = this.context.get()
        if (context != null) {
            val progressDialog = ProgressDialog(context)
            dialog = progressDialog
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            progressDialog.setCancelable(false)
            progressDialog.setMessage(context.getString(R.string.message_pleaseWait))
            progressDialog.setTitle(context.getString(R.string.title_format))
            progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, context.getString(R.string.button_cancel)) { _, _ -> cancel(true) }
            progressDialog.show()
        }
    }

    override fun onProgressUpdate(vararg values: Progress) {
        super.onProgressUpdate(*values)
        val progress = values[0]
        dialog?.let {
            if (progress.hasTitle()) it.setTitle(progress.title)
            if (progress.hasMax()) it.max = progress.max!!
            if (progress.hasProgress()) it.progress = progress.progress!!
        }
    }

    override fun doInBackground(vararg params: Model): Void? {
        for (i in params.indices) {
            if (params[i] is Script) {
                val script = params[i] as Script
                publishProgress(Progress(params.size, i, script.name))
                val code = beautify(script.text) ?: return null
                script.text = code
                MultiTool.get().doInLL { scriptService -> scriptService.updateScript(script.asLLScript()) }
            }
        }
        return null
    }

    override fun onPostExecute(result: Void) {
        if (dialog != null) {
            dialog!!.dismiss()
        }
        val context = this.context.get()
        if (context != null) {
            Toast.makeText(context, R.string.message_done, Toast.LENGTH_SHORT).show()
        }
    }

    private class CloseNotes {
        internal var prevLines: AstNode? = null
        internal var prevLinesDist = Integer.MAX_VALUE
        internal var sameLine: AstNode? = null
        internal var sameLineDist = Integer.MAX_VALUE
        internal var nextLines: AstNode? = null
        internal var nextLinesDist = Integer.MAX_VALUE
    }

    private class CommentNodeWrapper internal constructor(internal val node: AstNode, private val comment: Comment, private val pos: Position) : AstNode() {

        override fun toSource(depth: Int): String {
            return toSource(node, comment, pos, depth)
        }

        override fun visit(visitor: NodeVisitor) {
            node.visit(visitor)
        }

        override fun getType(): Int {
            return node.type
        }
    }

    private class CommentObjectLiteralWrapper internal constructor(internal val node: ObjectLiteral, private val comment: Comment, private val pos: Position) : ObjectLiteral() {

        override fun toSource(depth: Int): String {
            return toSource(node, comment, pos, depth)
        }

        override fun visit(visitor: NodeVisitor) {
            node.visit(visitor)
        }

        override fun getType(): Int {
            return node.type
        }
    }

    private class CommentSwitchCaseWrapper internal constructor(private val node: SwitchCase, private val comment: Comment, private val pos: Position) : SwitchCase() {

        override fun toSource(depth: Int): String {
            return toSource(node, comment, pos, depth)
        }

        override fun visit(visitor: NodeVisitor) {
            node.visit(visitor)
        }

        override fun getType(): Int {
            return node.type
        }
    }

    private enum class Position {
        BEFORE,
        IN,
        AFTER_SAME_LINE,
        AFTER_NEXT_LINE,
        ONLY_COMMENT
    }

    class Progress(val max: Int?, val progress: Int?, val title: String?) {

        fun hasTitle(): Boolean {
            return title != null
        }

        fun hasMax(): Boolean {
            return max != null
        }

        fun hasProgress(): Boolean {
            return progress != null
        }


    }

    companion object {

        private fun toSource(node: AstNode, comment: Comment, position: Position, depth: Int): String {
            var source = node.toSource(depth)
            when (position) {
                Position.BEFORE -> {
                    if (!source.startsWith("\n") && comment.commentType != Token.CommentType.BLOCK_COMMENT) {
                        source = "\n" + source
                    }
                    return "\n" + node.makeIndent(depth) + comment.value + source
                }
                Position.IN -> {
                    val i = source.indexOf('\n')
                    if (i != -1) {
                        return source.substring(0, i) + " " + comment.value + source.substring(i)
                    }
                    return if (source.endsWith("\n")) {
                        source.substring(0, source.length - 1) + " " + comment.value + "\n"
                    } else source + " " + comment.value
                }
                Position.AFTER_SAME_LINE -> {
                    return if (source.endsWith("\n")) {
                        source.substring(0, source.length - 1) + " " + comment.value + "\n"
                    } else source + " " + comment.value
                }
                Position.AFTER_NEXT_LINE -> {
                    if (!source.endsWith("\n")) {
                        source += "\n"
                    }
                    return source + node.makeIndent(depth) + comment.value + "\n"
                }
                Position.ONLY_COMMENT -> return comment.value
                else -> return source
            }
        }
    }
}
