package com.faendir.lightning_launcher.multitool.animation;

import android.graphics.PointF;
import android.view.View;
import android.view.ViewPropertyAnimator;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;

/**
 * @author lukas
 * @since 10.07.18
 */
class Transformation {
    PointF scale = new PointF(1, 1);
    PointF translate = new PointF(0, 0);
    PointF pivot = new PointF(0, 0);
    float turn = 0;
    PointF rotation = new PointF(0, 0);
    float alpha = 1;
    private boolean partial = true;

    void transform(Item item) {
        String pinMode = item.getProperties().getString(PropertySet.ITEM_PIN_MODE);
        PointB transform = new PointB(!pinMode.contains("X"), !pinMode.contains("Y"));
        if (partial ? transform.any() : transform.both()) {
            PointF center = AnimationScript.center(item, false);
            transform(item.getRootView(), transform, center);
        }
    }

    void transform(View view, PointB transform, PointF center) {
        view.setPivotX(center.x + pivot.x);
        view.setPivotY(center.y + pivot.y);
        ViewPropertyAnimator animator = view.animate().setDuration(0).alpha(alpha).rotation(turn * 360);
        if (transform.x) animator.scaleX(scale.x).translationX(translate.x).rotationX(rotation.x * 360);
        if (transform.y) animator.scaleY(scale.y).translationY(translate.y).rotationY(rotation.y * 360);
        animator.start();
    }

    Transformation onlyUnpinnedItems() {
        partial = false;
        return this;
    }
}
