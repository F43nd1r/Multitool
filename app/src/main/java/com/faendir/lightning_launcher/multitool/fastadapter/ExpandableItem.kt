package com.faendir.lightning_launcher.multitool.fastadapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.faendir.lightning_launcher.multitool.R
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.expandable.items.ModelAbstractExpandableItem
import com.mikepenz.fastadapter_extensions.swipe.ISwipeable

/**
 * Created by Lukas on 23.08.2015.
 * An item in the ExpandableListView
 */
class ExpandableItem<T : Model>(item: T, private val size: Int) : ModelAbstractExpandableItem<T, ExpandableItem<T>, ExpandableItem.ViewHolder, ExpandableItem<T>>(item), ISwipeable<ExpandableItem<T>, IItem<*, *>> {
    private var swipeable: Boolean = item is DeletableModel
    private var swipedAction: (()->Unit?)? = null

    private val level: Int
        get() {
            var level = 0
            var parent: ExpandableItem<*>? = parent
            while (parent != null) {
                parent = parent.parent
                level++
            }
            return level
        }

    init {
        if (item is ClickAwareModel) {
            withOnItemClickListener { _, _, _, _ ->
                (item as ClickAwareModel).onClick()
                true
            }
        }
    }

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun getType(): Int = R.id.txt

    override fun getLayoutRes(): Int = R.layout.list_item_app

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        val context = holder.text.context
        var icon: Drawable
        val text: String
        val background: Int
        if (swipedAction == null) {
            text = model.name
            icon = DrawableCompat.wrap(model.getIcon(context))
            DrawableCompat.setTint(icon, model.tintColor)
            DrawableCompat.setTintMode(icon, PorterDuff.Mode.MULTIPLY)
            background = if (isSelected) ContextCompat.getColor(context, R.color.accent) else Color.TRANSPARENT
        } else {
            text = (model as DeletableModel).getUndoText(context)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white)!!
            val ref = model.getIcon(context)
            val diffX = (ref.intrinsicWidth.toFloat() - icon.intrinsicWidth) / 2
            val diffY = (ref.intrinsicHeight.toFloat() - icon.intrinsicHeight) / 2
            icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                InsetDrawable(icon, diffX, diffY, diffX, diffY)
            } else {
                InsetDrawable(icon, Math.round(diffX), Math.round(diffY), Math.round(diffX), Math.round(diffY))
            }
            background = Color.RED
        }
        holder.text.text = text
        icon.setBounds(0, 0, size * icon.intrinsicWidth / icon.intrinsicHeight, size)
        holder.text.setCompoundDrawables(icon.mutate(), null, null, null)
        holder.text.setBackgroundColor(background)
        holder.applyInset(10 * level)
    }

    override fun toString(): String = "ExpandableItem{$model}"

    override fun isSwipeable(): Boolean = swipeable

    override fun withIsSwipeable(swipeable: Boolean): ExpandableItem<T> {
        this.swipeable = swipeable
        return this
    }

    fun setSwipedAction(action: (()->Unit?)?) {
        this.swipedAction = action
        withOnItemPreClickListener { _, _, _, _ -> (this.swipedAction != null).also { if (it) swipedAction?.invoke() } }
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.txt)
        private val basePadding: Int = itemView.paddingLeft

        internal fun applyInset(insetDp: Int) {
            val displayMetrics = itemView.context.resources.displayMetrics
            val inset = Math.round(insetDp * displayMetrics.density)
            itemView.setPadding(basePadding + inset, itemView.paddingTop, itemView.paddingRight, itemView.paddingBottom)
        }
    }
}
