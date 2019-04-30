package com.faendir.lightning_launcher.multitool.animation

import android.app.AlertDialog
import android.graphics.Point
import android.graphics.PointF
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.*
import com.faendir.lightning_launcher.multitool.util.Utils.GSON

/**
 * @author lukas
 * @since 09.07.18
 */
class AnimationScript(private val utils: Utils) : JavaScript.Setup, JavaScript.Normal {

    override fun setup() {
        val script = utils.installNormalScript()
        val container = utils.container
        utils.addEventHandler(container.properties, PropertySet.POSITION_CHANGED, EventHandler.RUN_SCRIPT, script.id.toString() + "/" + javaClass.name)
        val tag = container.getTag(TAG_ANIMATION)
        val config = if (tag != null) GSON.fromJson(tag, Config::class.java) else Config()
        AlertDialog.Builder(utils.lightningContext).setTitle(utils.getString(R.string.title_animationChooser))
                .setItems(Animation.values().map { it.label }.map { utils.getString(it) }.toTypedArray()) { _, which ->
                    config.animation = Animation.values()[which]
                    container.setTag(TAG_ANIMATION, GSON.toJson(config))
                }
                .setNegativeButton(utils.getString(R.string.button_disable)) { _, _ ->
                    config.animation = null
                    container.setTag(TAG_ANIMATION, GSON.toJson(config))
                }
                .show()
    }

    override fun run() {
        val container = utils.container
        val tag = container.getTag(TAG_ANIMATION) ?: return
        var config: Config? = null
        try {
            config = GSON.fromJson(tag, Config::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (config?.animation == null) return
        val containerSize = Size(container.width, container.height)
        val position = PointF(container.positionX, container.positionY)
        val activePage = pageOf(position, containerSize)
        val percent = PointF(position.x / containerSize.width - activePage.x, position.y / containerSize.height - activePage.y)
        for (item in container.allItems) {
            val center = center(item, true)
            val page = pageOf(center, containerSize)
            val onPage = PointB(page.x == activePage.x, page.y == activePage.y)
            if ((onPage.x || page.x == activePage.x + 1) && (onPage.y || page.y == activePage.y + 1)) {
                config.animation!!.getTransformation(percent, containerSize, makePageRelative(center, containerSize), onPage).transform(item)
            } else {
                Transformation().transform(item)
            }
        }
    }

    private fun pageOf(position: PointF, containerSize: Size): Point {
        return Point(Math.floor((position.x / containerSize.width).toDouble()).toInt(), Math.floor((position.y / containerSize.height).toDouble()).toInt())
    }

    private fun makePageRelative(point: PointF, containerSize: Size): PointF {
        return PointF(positiveModulo(point.x, containerSize.width), positiveModulo(point.y, containerSize.height))
    }

    private fun positiveModulo(i: Float, modulo: Int): Float {
        var result = i % modulo
        if (result < 0) result += modulo.toFloat()
        return result
    }

    private class Config {
        internal var animation: Animation? = null
    }

    companion object {
        private const val TAG_ANIMATION = "animation"

        fun center(item: Item, absolute: Boolean): PointF {
            val radius = item.rotation * Math.PI / 180
            val sine = Math.abs(Math.sin(radius))
            val cosine = Math.abs(Math.cos(radius))
            val width = (item.width * item.scaleX).toDouble()
            val height = (item.height * item.scaleY).toDouble()
            val result = PointF((width * cosine + height * sine).toFloat() / 2, (height * cosine + width * sine).toFloat() / 2)
            if (absolute) {
                result.x += item.positionX
                result.y += item.positionY
            }
            return result
        }
    }
}
