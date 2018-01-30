package com.faendir.lightning_launcher.multitool.fastadapter;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;

import com.faendir.lightning_launcher.multitool.R;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.expandable.items.ModelAbstractExpandableItem;
import com.mikepenz.fastadapter_extensions.swipe.ISwipeable;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Lukas on 23.08.2015.
 * An item in the ExpandableListView
 */
public class ExpandableItem<T extends Model> extends ModelAbstractExpandableItem<T, ExpandableItem<T>, ExpandableItem.ViewHolder, ExpandableItem<T>> implements ISwipeable<ExpandableItem<T>, IItem> {
    private boolean swipeable = true;
    private Runnable swipedAction;
    private final int size;

    public ExpandableItem(T item, int size) {
        super(item);
        swipeable = item instanceof DeletableModel;
        this.size = size;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.txt;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.list_item_app;
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        Context context = holder.text.getContext();
        Drawable icon;
        String text;
        int background;
        if (swipedAction == null) {
            text = getModel().getName();
            icon = DrawableCompat.wrap(getModel().getIcon(context));
            DrawableCompat.setTint(icon, getModel().getTintColor(context));
            DrawableCompat.setTintMode(icon, PorterDuff.Mode.MULTIPLY);
            background = isSelected() ? context.getResources().getColor(R.color.accent) : Color.TRANSPARENT;
        } else {
            text = ((DeletableModel) getModel()).getUndoText(context);
            icon = context.getResources().getDrawable(R.drawable.ic_delete_white);
            Drawable ref = getModel().getIcon(context);
            float diffX = ((float)ref.getIntrinsicWidth() - icon.getIntrinsicWidth()) / 2;
            float diffY = ((float)ref.getIntrinsicHeight() - icon.getIntrinsicHeight()) / 2;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                icon = new InsetDrawable(icon, diffX, diffY, diffX, diffY);
            } else {
                icon = new InsetDrawable(icon, Math.round(diffX), Math.round(diffY), Math.round(diffX), Math.round(diffY));
            }
            background = Color.RED;
        }
        holder.text.setText(text);
        icon.setBounds(0, 0, size * icon.getIntrinsicWidth() / icon.getIntrinsicHeight(), size);
        holder.text.setCompoundDrawables(icon.mutate(), null, null, null);
        holder.text.setBackgroundColor(background);
        holder.applyInset(10 * getLevel());
    }

    private int getLevel() {
        int level;
        ExpandableItem<?> parent = getParent();
        for (level = 0; parent != null; level++) {
            parent = parent.getParent();
        }
        return level;
    }

    @Override
    public String toString() {
        return "ExpandableItem{" + getModel().toString() + "}";
    }

    @Override
    public boolean isSwipeable() {
        return swipeable;
    }

    @Override
    public ExpandableItem<T> withIsSwipeable(boolean swipeable) {
        this.swipeable = swipeable;
        return this;
    }

    public void setSwipedAction(Runnable action) {
        this.swipedAction = action;
        withOnItemPreClickListener((v, adapter, item, position) -> {
            if (this.swipedAction != null) {
                swipedAction.run();
                return true;
            }
            return false;
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final int basePadding;
        public final AppCompatTextView text;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.txt);
            //setBackgroundDrawable(itemView, itemView.getContext().getResources().getColor(R.color.selector), itemView.getContext().getResources().getColor(R.color.accent));
            basePadding = itemView.getPaddingLeft();
        }

        void setBackgroundDrawable(View view, @ColorInt int highlightColor, @ColorInt int selectionColor) {
            Drawable background;
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(selectionColor));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                float[] outerRadii = new float[8];
                Arrays.fill(outerRadii, 3);
                RoundRectShape r = new RoundRectShape(outerRadii, null, null);
                ShapeDrawable shapeDrawable = new ShapeDrawable(r);
                shapeDrawable.getPaint().setColor(selectionColor);
                background = new RippleDrawable(ColorStateList.valueOf(highlightColor), drawable, shapeDrawable);
            } else {
                drawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(highlightColor));
                int duration = view.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
                drawable.setEnterFadeDuration(duration);
                drawable.setExitFadeDuration(duration);
                background = drawable;
            }
            drawable.addState(new int[0], view.getBackground());
            view.setBackgroundDrawable(background);
        }

        void applyInset(int insetDp) {
            DisplayMetrics displayMetrics = itemView.getContext().getResources().getDisplayMetrics();
            int inset = Math.round(insetDp * displayMetrics.density);
            itemView.setPadding(basePadding + inset, itemView.getPaddingTop(), itemView.getPaddingRight(), itemView.getPaddingBottom());
        }
    }

}
