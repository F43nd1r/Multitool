package com.faendir.lightning_launcher.multitool.launcherscript

import android.app.AlertDialog
import android.widget.*
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.animation.AnimationScript
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem
import com.faendir.lightning_launcher.multitool.fastadapter.ItemFactory
import com.faendir.lightning_launcher.multitool.fastadapter.Model
import com.faendir.lightning_launcher.multitool.proxy.*
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IInterceptor
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ModelAdapter
import com.mikepenz.fastadapter.expandable.ExpandableExtension
import com.mikepenz.fastadapter.utils.DefaultItemListImpl
import org.acra.util.StreamReader
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.util.*

/**
 * @author lukas
 * @since 04.07.18
 */
@Keep
class MultiToolScript(private val utils: Utils) : JavaScript.Normal {
    private val event: Event = utils.event

    override fun run() {
        val recyclerView = RecyclerView(utils.multitoolContext)
        recyclerView.layoutManager = LinearLayoutManager(utils.multitoolContext)
        val dialog = AlertDialog.Builder(utils.lightningContext).setView(recyclerView)
                .setCancelable(true)
                .setTitle(utils.getString(R.string.title_multitoolScript))
                .setNegativeButton(utils.getString(R.string.button_cancel)) { d, _ -> d.cancel() }
                .create()
        val factory = ItemFactory.forLauncherIconSize<Model>(utils.lightningContext)
        val information = factory.wrap(ActionGroup(utils.getString(R.string.group_information)))
        information.withSubItems(listOf(Action(utils.getString(R.string.action_event)) { showEventInfo(dialog) },
                Action(utils.getString(R.string.action_container)) { showContainerInfo(dialog) }, *(if  (event.item != null) arrayOf(Action(utils.getString(R.string.action_item)) { showItemInfo(dialog) },
                        Action(utils.getString(R.string.action_intent)) { showIntentInfo(dialog) },
                        Action(utils.getString(R.string.action_icon)) { showIconInfo(dialog) }) else arrayOf())).map(factory::wrap).toMutableList())
        val itemUtils = factory.wrap(ActionGroup(utils.getString(R.string.group_utils)))
        itemUtils.withSubItems(listOf(Action(utils.getString(R.string.action_attach)) { showAttachDetach(dialog) },
                Action(utils.getString(R.string.action_resize)) { showResizeDetached(dialog) },
                Action(utils.getString(R.string.action_delete)) { showDelete(dialog) }).map(factory::wrap).toMutableList())
        val other = factory.wrap(ActionGroup(utils.getString(R.string.group_other)))
        other.withSubItems(listOf(Action(utils.getString(R.string.action_resetTag)) { showResetTag(dialog) },
                Action(utils.getString(R.string.action_resetTool)) { showResetTool(dialog) },
                Action(utils.getString(R.string.action_save)) { save(dialog) },
                Action(utils.getString(R.string.action_deleteRecents)) { deleteHistory(dialog) }).map(factory::wrap).toMutableList())
        recyclerView.adapter = FastAdapter.with<IItem<Model, ExpandableItem.ViewHolder>, ModelAdapter<Model, ExpandableItem<Model>>>(ModelAdapter(DefaultItemListImpl(mutableListOf(information, itemUtils, other)), IInterceptor<Model, ExpandableItem<Model>>(factory::wrap)))
                .addExtension(ExpandableExtension<IItem<Model, ExpandableItem.ViewHolder>>())
        dialog.show()
    }

    private fun deleteHistory(dialog: AlertDialog) {
        dialog.dismiss()
        File(utils.lightningContext.filesDir.path + "/statistics").delete()
        Toast.makeText(utils.lightningContext, utils.getString(R.string.toast_recentsDeleted), Toast.LENGTH_SHORT).show()
    }

    private fun save(dialog: AlertDialog) {
        dialog.dismiss()
        utils.lightning.save()
        Toast.makeText(utils.lightningContext, utils.getString(R.string.toast_savedLayout), Toast.LENGTH_SHORT).show()
    }

    private fun showResetTool(dialog: AlertDialog) {
        dialog.dismiss()
        val cont = event.container
        val items = cont.allItems
        val listItems = arrayOf<CharSequence>(utils.getString(R.string.tool_cell), utils.getString(R.string.tool_position), utils.getString(R.string.tool_rotation), utils.getString(R.string.tool_scale), utils.getString(R.string.tool_skew), utils.getString(R.string.tool_size), utils.getString(R.string.tool_visibility))
        val bools = BooleanArray(listItems.size)
        AlertDialog.Builder(utils.lightningContext).setMultiChoiceItems(listItems, null) { _, which, isChecked -> bools[which] = isChecked }
                .setTitle(utils.getString(R.string.title_reset))
                .setCancelable(true)
                .setPositiveButton(utils.getString(R.string.button_confirm)) { _, _ ->
                    for (item in items) {
                        if (bools[0]) item.setCell(0, 0, 1, 1)
                        if (bools[1]) item.setPosition(0f, 0f)
                        if (bools[2]) item.rotation = 0f
                        if (bools[3]) item.setScale(1f, 1f)
                        if (bools[4]) item.setSkew(0f, 0f)
                        if (bools[5]) item.setSize(cont.cellWidth, cont.cellHeight)
                        if (bools[6]) item.setVisibility(true)
                    }
                }
                .show()
    }

    private fun showResetTag(dialog: AlertDialog) {
        dialog.dismiss()
        val container = event.container
        val item = event.item
        val (tags,  deleter) = when {
            item != null -> Pair<Set<String>, (String) -> Unit>(getTags(item).keys, { tag -> item.setTag(tag, null) })
            else -> Pair(getTags(container).keys, { tag -> container.setTag(tag, null) })
        }
        val options = tags.toMutableList()
        options.add(0, utils.getString(R.string.text_allTags))
        AlertDialog.Builder(utils.lightningContext).setTitle(utils.getString(R.string.title_tagChooser))
                .setCancelable(true)
                .setItems(options.toTypedArray<CharSequence>()) { _, which ->
                    val delete = if (which == 0) ArrayList(tags) else listOf(options[which])
                    for (tag in delete) {
                        deleter.invoke(tag)
                    }
                    Toast.makeText(utils.lightningContext, utils.getString(R.string.toast_tagsDeleted), Toast.LENGTH_SHORT).show()
                    utils.lightning.save()
                }
                .setNegativeButton(utils.getString(R.string.button_cancel), null)
                .show()
    }

    private fun showDelete(dialog: AlertDialog) {
        dialog.dismiss()
        AlertDialog.Builder(utils.lightningContext).setTitle(utils.getString(R.string.title_deleteAll))
                .setMessage(utils.getString(R.string.text_areYouSure))
                .setPositiveButton(utils.getString(R.string.button_confirm)) { _, _ ->
                    val container = event.container
                    container.allItems.forEach(container::removeItem)
                }
                .setNegativeButton(utils.getString(R.string.button_cancel), null)
                .show()
    }

    private fun showResizeDetached(dialog: AlertDialog) {
        dialog.dismiss()
        val linearLayout = LinearLayout(utils.lightningContext)
        val c = event.container
        linearLayout.orientation = LinearLayout.VERTICAL
        val widthText = TextView(utils.lightningContext)
        widthText.text = utils.getString(R.string.text_width)
        linearLayout.addView(widthText)
        val widthPicker = NumberPicker(utils.lightningContext)
        widthPicker.minValue = 1
        widthPicker.maxValue = 9999
        widthPicker.value = c.cellWidth.toInt()
        linearLayout.addView(widthPicker)
        val heightText = TextView(utils.lightningContext)
        heightText.text = utils.getString(R.string.text_height)
        linearLayout.addView(heightText)
        val heightPicker = NumberPicker(utils.lightningContext)
        heightPicker.minValue = 1
        heightPicker.maxValue = 9999
        heightPicker.value = c.cellHeight.toInt()
        linearLayout.addView(heightPicker)
        AlertDialog.Builder(utils.lightningContext).setView(linearLayout)
                .setCancelable(true)
                .setTitle(utils.getString(R.string.title_size))
                .setPositiveButton(utils.getString(R.string.button_confirm)) { _, _ ->
                    val width = widthPicker.value.toFloat()
                    val height = heightPicker.value.toFloat()
                    c.allItems.forEach { it.setSize(width, height) }
                }
                .setNegativeButton(utils.getString(R.string.button_cancel), null)
                .show()
    }

    private fun showAttachDetach(dialog: AlertDialog) {
        dialog.dismiss()
        AlertDialog.Builder(utils.lightningContext)
                .setTitle(utils.getString(R.string.script_name))
                .setMessage(utils.getString(R.string.text_attach))
                .setCancelable(true)
                .setPositiveButton(utils.getString(R.string.button_attach)) { _, _ -> attachDetach(true) }
                .setNegativeButton(utils.getString(R.string.button_detach)) { _, _ -> attachDetach(false) }
                .setNeutralButton(utils.getString(R.string.button_cancel), null)
                .show()
    }

    private fun attachDetach(attach: Boolean) {
        event.container.allItems.forEach { it.properties.edit().setBoolean(PropertySet.ITEM_ON_GRID, attach).commit() }
        Toast.makeText(utils.lightningContext, utils.getString(R.string.toast_done), Toast.LENGTH_SHORT).show()
    }

    private fun showIconInfo(dialog: AlertDialog) {
        dialog.dismiss()
        val it = event.item
        //create view structure
        val root = LinearLayout(utils.lightningContext)
        root.orientation = LinearLayout.VERTICAL

        //check for all kinds of images in this item and add them to the view if there are any
        addImageIfNotNull(root, it.getBoxBackground(Box.MODE_NORMAL), utils.getString(R.string.text_normalBox))
        addImageIfNotNull(root, it.getBoxBackground(Box.MODE_SELECTED), utils.getString(R.string.text_selectedBox))
        addImageIfNotNull(root, it.getBoxBackground(Box.MODE_FOCUSED), utils.getString(R.string.text_focusedBox))
        if (Item.TYPE_SHORTCUT == it.type) {
            val shortcut = ProxyFactory.cast(it, Shortcut::class.java)
            addImageIfNotNull(root, shortcut.defaultIcon, utils.getString(R.string.text_defaultIcon))
            addImageIfNotNull(root, shortcut.customIcon, utils.getString(R.string.text_customIcon))
        }
        if (root.childCount <= 0) {
            Toast.makeText(utils.lightningContext, utils.getString(R.string.toast_noImages), Toast.LENGTH_SHORT).show() //no image found
            return
        }
        //at least one image found
        val scroll = ScrollView(utils.lightningContext)
        scroll.addView(root)
        AlertDialog.Builder(utils.lightningContext).setView(scroll)
                .setCancelable(true)
                .setTitle(utils.getString(R.string.title_icon))
                .setNeutralButton(utils.getString(R.string.button_close)) { d, _ -> d.dismiss() }
                .show()
    }

    private fun addImageIfNotNull(root: LinearLayout, image: Image?, txt: String) {
        if (image != null) {
            val textView = TextView(utils.lightningContext)
            textView.text = utils.getString(R.string.text_imageInfo, txt, image.width, image.height)
            root.addView(textView)
            if (Image.TYPE_BITMAP == image.type) {
                val imageView = ImageView(utils.lightningContext)
                imageView.setImageBitmap(ProxyFactory.cast(image, ImageBitmap::class.java).bitmap)
                root.addView(imageView)
            }
        }
    }

    private fun showIntentInfo(dialog: AlertDialog) {
        dialog.dismiss()
        val it = event.item
        if (it == null || Item.TYPE_SHORTCUT != it.type) {
            Toast.makeText(utils.lightningContext, utils.getString(R.string.toast_noIntent), Toast.LENGTH_SHORT).show()
            return
        }
        val intent = ProxyFactory.cast(it, Shortcut::class.java).intent
        intent.getStringExtra("somenamenoonewouldeveruse")
        showText(utils.getString(R.string.text_intentInfo, intent, intent.extras), utils.getString(R.string.title_intentInfo))
    }

    private fun showItemInfo(dialog: AlertDialog) {
        dialog.dismiss()
        val i = event.item
        if (i == null) {
            Toast.makeText(utils.lightningContext, utils.getString(R.string.toast_noItem), Toast.LENGTH_SHORT).show()
            return
        }

        val tags = getTags(i).entries.joinToString("\n") { entry -> entry.key + ": " + entry.value }
        val label = when (i.type) {
            Item.TYPE_SHORTCUT, Item.TYPE_FOLDER -> ProxyFactory.cast(i, Shortcut::class.java).label
            else -> ""
        }
        showText(utils.getString(R.string.text_itemInfo,
                label,
                i.name,
                i.type,
                i.id,
                i.width,
                i.height,
                i.positionX,
                i.positionY,
                i.scaleX,
                i.scaleY,
                i.rotation,
                AnimationScript.center(i, true),
                i.cell,
                i.isVisible,
                tags), utils.getString(R.string.title_itemInfo))
    }

    private fun getTags(item: Item): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        try {
            val s = StreamReader(utils.lightningContext.filesDir.path + "/pages/" + item.parent.id + "/items").read()
            val all = JSONObject(s).getJSONArray("i")
            var x = 0
            var jsonItem = JSONObject()
            while (x < all.length()) {
                jsonItem = all.getJSONObject(x)
                if (jsonItem.getInt("b") == item.id) break
                x++
            }
            if (x != all.length()) {
                val jsonTags = jsonItem.getJSONObject("an")
                val iterator = jsonTags.keys()
                while (iterator.hasNext()) {
                    val property = iterator.next()
                    result[property] = jsonTags.getString(property)
                }
            } else {
                Toast.makeText(utils.lightningContext, utils.getString(R.string.toast_noTags), Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }

    private fun getTags(container: Container): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        result["_"] = container.tag
        try {
            val s = StreamReader(utils.lightningContext.filesDir.path + "/pages/" + container.id + "/conf").read()
            val data = JSONObject(s)
            if (data.has("tags")) {
                val jsonTags = data.getJSONObject("tags")
                val iterator = jsonTags.keys()
                while (iterator.hasNext()) {
                    val property = iterator.next()
                    result[property] = jsonTags.getString(property)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return result
    }

    private fun showContainerInfo(dialog: AlertDialog) {
        dialog.dismiss()
        val c = event.container
        val t = c.type //Differentiate between Desktop and other containers

        //read Tags from launcher file
        val tags = getTags(c).entries.joinToString("\n") { entry -> entry.key + ": " + entry.value }
        val name = when (t) {
            Container.TYPE_DESKTOP -> ProxyFactory.cast(c, Desktop::class.java).name
            Container.TYPE_FOLDER -> ProxyFactory.cast(c.opener, Shortcut::class.java).label
            else -> c.opener.name
        }
        showText(utils.getString(R.string.text_containerInfo,
                t,
                name,
                c.id,
                c.width,
                c.height,
                c.boundingBox,
                c.cellWidth,
                c.cellHeight,
                c.positionX,
                c.positionY,
                c.positionScale,
                tags,
                Arrays.toString(c.allItems)), utils.getString(R.string.title_containerInfo))
    }

    private fun showEventInfo(dialog: AlertDialog) {
        dialog.dismiss()
        val event = this.event
        var touchX = 0f
        var touchY = 0f
        var touchScreenX = 0f
        var touchScreenY = 0f
        try {
            //test if event contains touch data
            touchX = event.touchX
            touchY = event.touchY
            touchScreenX = event.touchScreenX
            touchScreenY = event.touchScreenY
        } catch (ignored: Exception) {
        }

        showText(utils.getString(R.string.text_eventInfo,
                event.source,
                DateFormat.getInstance().format(event.date),
                event.container,
                event.screen,
                event.item,
                event.data,
                touchX,
                touchY,
                touchScreenX,
                touchScreenY), utils.getString(R.string.title_eventInfo))
    }

    private fun showText(text: String, title: String) {
        AlertDialog.Builder(utils.lightningContext).setTitle(title)
                .setMessage(text)
                .setCancelable(true)
                .setNeutralButton(utils.getString(R.string.button_close)) { dialog, _ -> dialog.dismiss() }
                .show()
    }
}
