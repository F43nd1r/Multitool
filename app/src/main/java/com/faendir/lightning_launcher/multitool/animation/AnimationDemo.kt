package com.faendir.lightning_launcher.multitool.animation

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.faendir.lightning_launcher.multitool.R

/**
 * @author lukas
 * @since 19.07.18
 */
class AnimationDemo(context: Context, attrs: AttributeSet) : HorizontalScrollView(context, attrs) {
    private val view1: ImageView
    private val view2: ImageView
    private val center: PointF
    private lateinit var size: Size
    private val percent: PointF
    private var animation: Animation? = null

    init {
        val layout = LinearLayout(context)
        layout.layoutParams = LayoutParams(0, 0)
        layout.orientation = LinearLayout.HORIZONTAL
        view1 = AppCompatImageView(context)
        view1.setLayoutParams(LinearLayout.LayoutParams(0, 0))
        view1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.animation_demo))
        view1.setBackgroundColor(Color.WHITE)
        DrawableCompat.setTint(view1.getDrawable(), ContextCompat.getColor(context, R.color.accent))
        view2 = AppCompatImageView(context)
        view2.setLayoutParams(LinearLayout.LayoutParams(0, 0))
        view2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.animation_demo_flipped))
        view2.setBackgroundColor(Color.BLACK)
        DrawableCompat.setTint(view2.getDrawable(), ContextCompat.getColor(context, R.color.accent))
        layout.addView(view1)
        layout.addView(view2)
        center = PointF(0f, 0f)
        percent = PointF(0f, 0f)
        addView(layout)
        post {
            size = Size(width, height)
            layout.layoutParams.width = size.width * 2
            layout.layoutParams.height = size.height
            view1.getLayoutParams().width = size.width
            view1.getLayoutParams().height = size.height
            view2.getLayoutParams().width = size.width
            view2.getLayoutParams().height = size.height
            center.x = (size.width / 2).toFloat()
            center.y = (size.height / 2).toFloat()
            requestLayout()
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (animation != null) {
            percent.x = l.toFloat() / measuredWidth
            if (percent.x < 0) percent.x = 0f
            if (percent.x > 1) percent.x = 1f
            animation!!.getTransformation(percent, size, center, LEFT_PAGE).transform(view1, TRANSFORM_ALL, center)
            animation!!.getTransformation(percent, size, center, RIGHT_PAGE).transform(view2, TRANSFORM_ALL, center)
        }
    }

    fun setAnimation(animation: Animation) {
        this.animation = animation
    }

    companion object {
        private val LEFT_PAGE = PointB(x = true, y = true)
        private val RIGHT_PAGE = PointB(x = false, y = true)
        private val TRANSFORM_ALL = PointB(x = true, y = true)
    }
}
