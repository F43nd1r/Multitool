package com.faendir.lightning_launcher.multitool.animation

import android.graphics.PointF
import android.view.View
import com.faendir.lightning_launcher.multitool.proxy.Item
import com.faendir.lightning_launcher.multitool.proxy.PropertySet

/**
 * @author lukas
 * @since 10.07.18
 */
class Transformation {
    var scale = PointF(1f, 1f)
    var translate = PointF(0f, 0f)
    var pivot = PointF(0f, 0f)
    var turn = 0f
    var rotation = PointF(0f, 0f)
    var alpha = 1f
    private var partial = true

    fun transform(item: Item) {
        val pinMode = item.properties.getString(PropertySet.ITEM_PIN_MODE)
        val transform = PointB(!pinMode.contains("X"), !pinMode.contains("Y"))
        if (if (partial) transform.any() else transform.both()) {
            val center = AnimationScript.center(item, false)
            transform(item.rootView, transform, center)
        }
    }

    fun transform(view: View, transform: PointB, center: PointF) {
        view.pivotX = center.x + pivot.x
        view.pivotY = center.y + pivot.y
        val animator = view.animate().setDuration(0).alpha(alpha).rotation(turn * 360)
        if (transform.x) animator.scaleX(scale.x).translationX(translate.x).rotationX(rotation.x * 360)
        if (transform.y) animator.scaleY(scale.y).translationY(translate.y).rotationY(rotation.y * 360)
        animator.start()
    }

    fun onlyUnpinnedItems(): Transformation {
        partial = false
        return this
    }
}
