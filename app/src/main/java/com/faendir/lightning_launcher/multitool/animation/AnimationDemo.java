package com.faendir.lightning_launcher.multitool.animation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.faendir.lightning_launcher.multitool.R;

/**
 * @author lukas
 * @since 19.07.18
 */
public class AnimationDemo extends HorizontalScrollView {
    private static final PointB LEFT_PAGE = new PointB(true, true);
    private static final PointB RIGHT_PAGE = new PointB(false, true);
    private static final PointB TRANSFORM_ALL = new PointB(true, true);
    private final ImageView view1;
    private final ImageView view2;
    private final PointF center;
    private final Size size;
    private final PointF percent;
    private Animation animation;

    public AnimationDemo(Context context, AttributeSet attrs) {
        super(context, attrs);
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new HorizontalScrollView.LayoutParams(0, 0));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        view1 = new AppCompatImageView(context);
        view1.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        view1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.animation_demo));
        view1.setBackgroundColor(Color.WHITE);
        DrawableCompat.setTint(view1.getDrawable(), ContextCompat.getColor(context, R.color.accent));
        view2 = new AppCompatImageView(context);
        view2.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        view2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.animation_demo_flipped));
        view2.setBackgroundColor(Color.BLACK);
        DrawableCompat.setTint(view2.getDrawable(), ContextCompat.getColor(context, R.color.accent));
        layout.addView(view1);
        layout.addView(view2);
        center = new PointF(0, 0);
        size = new Size(1, 1);
        percent = new PointF(0, 0);
        addView(layout);
        post(() -> {
            size.width = getWidth();
            size.height = getHeight();
            layout.getLayoutParams().width = size.width * 2;
            layout.getLayoutParams().height = size.height;
            view1.getLayoutParams().width = size.width;
            view1.getLayoutParams().height = size.height;
            view2.getLayoutParams().width = size.width;
            view2.getLayoutParams().height = size.height;
            center.x = size.width / 2;
            center.y = size.height / 2;
            requestLayout();
        });
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (animation != null) {
            percent.x = (float) l / getMeasuredWidth();
            if (percent.x < 0) percent.x = 0;
            if (percent.x > 1) percent.x = 1;
            animation.getTransformation(percent, size, center, LEFT_PAGE).transform(view1, TRANSFORM_ALL, center);
            animation.getTransformation(percent, size, center, RIGHT_PAGE).transform(view2, TRANSFORM_ALL, center);
        }
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }
}
