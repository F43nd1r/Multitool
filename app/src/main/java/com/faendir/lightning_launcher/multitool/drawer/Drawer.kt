package com.faendir.lightning_launcher.multitool.drawer

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.*
import java.util.*
import kotlin.collections.HashSet

/**
 * @author lukas
 * @since 08.07.18
 */
@Keep
class Drawer(private val utils: Utils) : JavaScript.Setup, JavaScript.CreateMenu, JavaScript.Normal {
    private val pm: PackageManager = utils.lightningContext.packageManager

    private val presentActivities: MutableList<ComponentName>
        get() = getItemsDeep(utils.container).mapNotNull { item -> item.getTag(TAG_INTENT) }
                .map { ComponentName.unflattenFromString(it) }
                .toMutableList()

    private val currentActivities: List<ResolveInfo>
        get() {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            return pm.queryIntentActivities(intent, 0)
        }

    override fun setup() {
        val script = utils.installNormalScript()
        val menu = utils.installMenuScript()
        val size = 500
        val d = utils.container
        val panel = d.addPanel(0f, 0f, size.toFloat(), size.toFloat())
        panel.properties.edit()
                .setBoolean(PropertySet.ITEM_ON_GRID, false)
                .also { it.getBox(PropertySet.ITEM_BOX).setColor(Box.border(), Box.MODE_ALL, 0x00000000) }
                .commit()
        panel.setSize(size.toFloat(), size.toFloat())
        panel.container.properties
                .edit()
                .setEventHandler(PropertySet.RESUMED, EventHandler.RUN_SCRIPT, script.id.toString() + "/" + javaClass.name)
                .setEventHandler(PropertySet.ITEM_MENU, EventHandler.RUN_SCRIPT, menu.id.toString() + "/" + javaClass.name)
                .setInteger(PropertySet.GRID_PORTRAIT_COLUMN_NUM, 3)
                .setInteger(PropertySet.GRID_PORTRAIT_ROW_NUM, 2)
                .setInteger(PropertySet.GRID_LANDSCAPE_COLUMN_NUM, 3)
                .setInteger(PropertySet.GRID_LANDSCAPE_ROW_NUM, 2)
                .commit()
        utils.centerOnTouch(panel)
    }

    override fun showMenu(menu: Menu, item: Item) {
        val mode = menu.mode
        if (mode == Menu.MODE_ITEM_NO_EM || mode == Menu.MODE_ITEM_EM) {
            menu.addMainItem(utils.getString(R.string.menu_hide), utils.asFunction { this.hide(menu, item) })
        }
    }

    private fun hide(menu: Menu, item: Item) {
        menu.close()
        val name = item.getTag(TAG_INTENT)
        if (name != null) {
            val hidden = HashSet(utils.sharedPref.getStringSet(utils.getString(R.string.pref_hiddenApps), emptySet())!!)
            if (!hidden.contains(name)) {
                hidden.add(name)
                utils.sharedPref.edit().putStringSet(utils.getString(R.string.pref_hiddenApps), hidden).apply()
                item.parent.removeItem(item)
                utils.installNormalScript().run(utils.activeScreen, javaClass.name)
            }
        }
    }

    override fun run() {
        val old = presentActivities
        val current = currentActivities
        val prefs = utils.sharedPref
        val hidden = prefs.getStringSet(utils.getString(R.string.pref_hiddenApps), emptySet())!!
        for (app in current) {
            val activity = app.activityInfo
            val name = ComponentName(activity.packageName, activity.name)
            if (hidden.contains(name.flattenToString())) {
                continue
            }
            if (old.contains(name)) {
                old.remove(name)
                continue
            }
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.setClassName(activity.packageName, activity.name)
            val item = utils.container.addShortcut(app.loadLabel(pm).toString(), intent, 0f, 0f)
            item.setTag(TAG_INTENT, name.flattenToString())
            val bmp = toBitmap(app.loadIcon(pm))
            val img = utils.imageClass.createImage(bmp.width, bmp.height)
            img.draw().drawBitmap(bmp, 0f, 0f, null)
            item.defaultIcon = img
        }
        for (name in old) {
            val flat = name.flattenToString()
            getItemsDeep(utils.container).forEach { item ->
                val tag = item.getTag(TAG_INTENT)
                if (tag == flat) {
                    item.parent.removeItem(item)
                }
            }
        }
        if (prefs.getBoolean(utils.getString(R.string.pref_keepSorted), true)) {
            Handler(utils.lightningContext.mainLooper).post { deepSort(utils.container) }
        }
    }

    private fun getItemsDeep(container: Container): List<Shortcut> {
        return container.allItems.flatMap {
            when (it.type) {
                Item.TYPE_PANEL -> {
                    try {
                        getItemsDeep(ProxyFactory.cast(it, Panel::class.java).container)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList<Shortcut>()
                    }
                }
                Item.TYPE_FOLDER -> getItemsDeep(ProxyFactory.cast(it, Folder::class.java).container)
                Item.TYPE_SHORTCUT -> listOf(ProxyFactory.cast(it, Shortcut::class.java))
                else -> emptyList()
            }

        }
    }

    private fun toBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    private fun deepSort(container: Container) {
        val items = container.allItems
        val matrix = Matrix()
        val move = ArrayList<Shortcut>()
        for (item in items) {
            when (item.type) {
                Item.TYPE_PANEL -> deepSort(ProxyFactory.cast(item, Panel::class.java).container)
                Item.TYPE_FOLDER -> deepSort(ProxyFactory.cast(item, Folder::class.java).container)
                Item.TYPE_SHORTCUT -> {
                    val tag = item.getTag(TAG_INTENT)
                    if (tag != null) move.add(ProxyFactory.cast(item, Shortcut::class.java))
                }
            }

            if (!move.contains(item) && item.properties.getBoolean(PropertySet.ITEM_ON_GRID)) {
                val cell = item.cell
                for (x in cell.left until cell.right) {
                    for (y in cell.top until cell.bottom) {
                        matrix.mark(x, y)
                    }
                }
            }
        }
        move.sortBy { it.label }
        val width = Math.round(container.width / container.cellWidth)
        var x = 0
        var y = 0
        for (item in move) {
            while (matrix[x, y]) {
                if (++x >= width) {
                    x = 0
                    y++
                }
            }
            matrix.mark(x, y)
            item.setCell(x, y, x + 1, y + 1, true)
        }
    }

    private class Matrix {
        private val list: MutableList<MutableList<Boolean>>

        init {
            list = ArrayList()
        }

        internal fun mark(x: Int, y: Int) {
            while (y >= list.size) {
                list.add(ArrayList())
            }
            val row = list[y]
            while (x >= row.size) {
                row.add(false)
            }
            row[x] = true
        }

        internal operator fun get(x: Int, y: Int): Boolean {
            if (y < list.size) {
                val row = list[y]
                if (x < row.size) {
                    return row[x]
                }
            }
            return false
        }
    }

    companion object {
        private const val TAG_INTENT = "intent"
    }
}
