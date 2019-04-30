package com.faendir.lightning_launcher.multitool.animation

import android.graphics.PointF
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.faendir.lightning_launcher.multitool.R

/**
 * @author lukas
 * @since 10.07.18
 */
@Keep
enum class Animation(@StringRes val label: Int) {
    BULLDOZE(R.string.title_bulldoze) {
        override fun getTransformation(percent: PointF, containerSize: Size, center: PointF, isStart: PointB): Transformation {
            val result = Transformation()
            result.scale.x = if (isStart.x) 1 - percent.x else percent.x
            result.scale.y = if (isStart.y) 1 - percent.y else percent.y
            result.translate.x = if (isStart.x) (containerSize.width - center.x) * percent.x else center.x * (percent.x - 1)
            result.translate.y = if (isStart.y) (containerSize.height - center.y) * percent.y else center.y * (percent.y - 1)
            return result
        }
    },
    CARD(R.string.title_card) {
        override fun getTransformation(percent: PointF, containerSize: Size, center: PointF, isStart: PointB): Transformation {
            val result = Transformation()
            if (!isStart.x && isStart.y) {
                result.translate.x = containerSize.width * (percent.x - 1)
                result.alpha = percent.x
            } else if (isStart.x && !isStart.y) {
                result.translate.y = containerSize.height * (percent.y - 1)
                result.alpha = percent.y
            }
            return result
        }
    },
    FLIP(R.string.title_flip) {
        override fun getTransformation(percent: PointF, containerSize: Size, center: PointF, isStart: PointB): Transformation {
            val result = Transformation()
            if (isStart.x != percent.x >= 0.5) {
                result.scale.x = if (isStart.x) 1 - percent.x * 2 else percent.x * 2 - 1
                result.translate.x = if (isStart.x) 2f * percent.x * (containerSize.width - center.x) else 2f * (percent.x - 1) * center.x
            } else {
                result.alpha = 0f
            }
            if (isStart.y != percent.y >= 0.5) {
                result.scale.y = if (isStart.y) 1 - percent.y * 2 else percent.y * 2 - 1
                result.translate.y = if (isStart.y) 2f * percent.y * (containerSize.height - center.y) else 2f * center.y * (percent.y - 1)
            } else {
                result.alpha = 0f
            }
            return result
        }
    },
    FLIP_3D(R.string.title_flip3d) {
        override fun getTransformation(percent: PointF, containerSize: Size, center: PointF, isStart: PointB): Transformation {
            val result = Transformation()
            if (isStart.x != percent.x >= 0.5) {
                val x = if (isStart.x) percent.x else percent.x - 1
                result.rotation.y = -x / 2
                result.translate.x = x * containerSize.width
                result.pivot.x = -center.x + containerSize.width / 2
            } else {
                result.alpha = 0f
            }
            if (isStart.y != percent.y >= 0.5) {
                val y = if (isStart.y) percent.y else percent.y - 1
                result.rotation.x = y / 2
                result.translate.y = y * containerSize.height
                result.pivot.y = -center.y + containerSize.height / 2
            } else {
                result.alpha = 0f
            }
            return result
        }
    },
    SHRINK(R.string.title_shrink) {
        override fun getTransformation(percent: PointF, containerSize: Size, center: PointF, isStart: PointB): Transformation {
            val result = Transformation().onlyUnpinnedItems()
            if (Math.abs(percent.x - 0.5) <= Math.abs(percent.y - 0.5)) {
                val x = (if (isStart.x) percent.x else 1 - percent.x) * 0.75f
                result.scale.y = 1 - x
                result.scale.x = result.scale.y
                result.translate.x = (containerSize.width - center.x) * x
                result.translate.y = (containerSize.height / 2 - center.y) * x
            } else {
                val y = (if (isStart.y) percent.y else 1 - percent.y) * 0.75f
                result.scale.y = 1 - y
                result.scale.x = result.scale.y
                result.translate.x = (containerSize.width / 2 - center.x) * y
                result.translate.y = (containerSize.height - center.y) * y
            }
            return result
        }
    },
    TURN(R.string.title_turn) {
        override fun getTransformation(percent: PointF, containerSize: Size, center: PointF, isStart: PointB): Transformation {
            val result = Transformation().onlyUnpinnedItems()
            if (Math.abs(percent.x - 0.5) <= Math.abs(percent.y - 0.5)) {
                if (isStart.y) {
                    val x = if (isStart.x) percent.x else percent.x - 1
                    result.turn = x / 4
                    result.translate.x = x * containerSize.width
                }
            } else {
                if (isStart.x) {
                    val y = if (isStart.y) percent.y else percent.y - 1
                    result.turn = -y / 4
                    result.translate.y = y * containerSize.height
                }
            }
            result.pivot.x = -center.x
            result.pivot.y = -center.y
            return result
        }
    },
    CUBE(R.string.title_cube) {
        override fun getTransformation(percent: PointF, containerSize: Size, center: PointF, isStart: PointB): Transformation {
            val result = Transformation().onlyUnpinnedItems()
            if (Math.abs(percent.x - 0.5) <= Math.abs(percent.y - 0.5)) {
                val x = if (isStart.x) percent.x else percent.x - 1
                result.rotation.y = -x / 4
                result.pivot.y = -center.y + containerSize.height / 2
                result.pivot.x = -center.x
                if (isStart.x) {
                    result.pivot.x += containerSize.width.toFloat()
                }
            } else {
                val y = if (isStart.y) percent.y else percent.y - 1
                result.rotation.x = y / 4
                result.pivot.x = -center.x + containerSize.width / 2
                result.pivot.y = -center.y
                if (isStart.y) {
                    result.pivot.y += containerSize.height.toFloat()
                }
            }
            return result
        }
    };

    abstract fun getTransformation(percent: PointF, containerSize: Size, center: PointF, isStart: PointB): Transformation
}
