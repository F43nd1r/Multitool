package com.faendir.lightning_launcher.multitool.music

import android.content.Intent
import android.widget.Toast
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.BuildConfig
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.*

/**
 * @author lukas
 * @since 05.07.18
 */
@Keep
class MusicSetup(private val utils: Utils) : JavaScript.Setup, JavaScript.Direct {

    override fun setup() {
        val resume = utils.installRegisterScript()
        val pause = utils.installUnregisterScript()
        val command = utils.installCommandScript()
        val launch = utils.installNormalScript()
        val size = 500
        val container = utils.container
        val panel = container.addPanel(0f, 0f, size.toFloat(), size.toFloat())
        panel.properties.edit()
                .setBoolean(PropertySet.ITEM_ON_GRID, false)
                .also { it.getBox(PropertySet.ITEM_BOX).setColor(Box.border(), Box.MODE_ALL, 0x00000000) }
                .commit()
        panel.setSize(size.toFloat(), size.toFloat())
        val p = panel.container
        p.properties
                .edit()
                .setEventHandler(PropertySet.RESUMED, EventHandler.RUN_SCRIPT, resume.id.toString() + "/" + MusicListener.LightningMusicListener::class.java.name)
                .setEventHandler(PropertySet.PAUSED, EventHandler.RUN_SCRIPT, pause.id.toString())
                .setString(PropertySet.SCROLLING_DIRECTION, PropertySet.SCROLLING_DIRECTION_NONE)
                .setInteger(PropertySet.GRID_PORTRAIT_COLUMN_NUM, 3)
                .setInteger(PropertySet.GRID_PORTRAIT_ROW_NUM, 10)
                .setInteger(PropertySet.GRID_LANDSCAPE_COLUMN_NUM, 3)
                .setInteger(PropertySet.GRID_LANDSCAPE_ROW_NUM, 10)
                .commit()
        val albumArt = p.addShortcut("albumart", Intent(), 0f, 0f)
        albumArt.name = ITEM_ALBUM_ART
        val title = p.addShortcut("title", Intent(), 0f, 0f)
        val album = p.addShortcut("album", Intent(), 0f, 0f)
        val artist = p.addShortcut("artist", Intent(), 0f, 0f)
        val play = p.addShortcut("Play/Pause", Intent(), 0f, 0f)
        val next = p.addShortcut("Next", Intent(), 0f, 0f)
        val previous = p.addShortcut("Previous", Intent(), 0f, 0f)
        albumArt.properties.edit()
                .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, false)
                .setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false)
                .setEventHandler(PropertySet.ITEM_TAP, EventHandler.RUN_SCRIPT, launch.id.toString() + "/" + MusicListener.LightningMusicListener::class.java.name)
                .also { it.getBox(PropertySet.ITEM_BOX).setColor(Box.CONTENT, Box.MODE_SELECTED, -0x1) }
                .commit()
        title.properties.edit().setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false).setBoolean(PropertySet.ITEM_ENABLED, false).commit()
        album.properties.edit().setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false).setBoolean(PropertySet.ITEM_ENABLED, false).commit()
        artist.properties.edit().setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false).setBoolean(PropertySet.ITEM_ENABLED, false).commit()
        play.properties
                .edit()
                .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, false)
                .setEventHandler(PropertySet.ITEM_TAP, EventHandler.RUN_SCRIPT, command.id.toString() + "/" + PLAY)
                .commit()
        play.defaultIcon = utils.imageClass.createImage(BuildConfig.APPLICATION_ID, utils.multitoolResources.getResourceName(R.drawable.ic_play))
        next.properties
                .edit()
                .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, false)
                .setEventHandler(PropertySet.ITEM_TAP, EventHandler.RUN_SCRIPT, command.id.toString() + "/" + NEXT)
                .commit()
        next.defaultIcon = utils.imageClass.createImage(BuildConfig.APPLICATION_ID, utils.multitoolResources.getResourceName(R.drawable.ic_next))
        previous.properties
                .edit()
                .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, false)
                .setEventHandler(PropertySet.ITEM_TAP, EventHandler.RUN_SCRIPT, command.id.toString() + "/" + PREVIOUS)
                .commit()
        previous.defaultIcon = utils.imageClass.createImage(BuildConfig.APPLICATION_ID, utils.multitoolResources.getResourceName(R.drawable.ic_previous))
        title.setBinding(PropertySet.SHORTCUT_LABEL, "$$VARIABLE_TITLE", true)
        album.setBinding(PropertySet.SHORTCUT_LABEL, "$$VARIABLE_ALBUM", true)
        artist.setBinding(PropertySet.SHORTCUT_LABEL, "$$VARIABLE_ARTIST", true)
        albumArt.setCell(0, 0, 3, 10, true)
        title.setCell(0, 0, 3, 1, true)
        album.setCell(0, 1, 3, 2, true)
        artist.setCell(0, 2, 3, 3, true)
        play.setCell(1, 7, 2, 10, true)
        next.setCell(2, 7, 3, 10, true)
        previous.setCell(0, 7, 1, 10, true)
        utils.centerOnTouch(panel)
        utils.lightning.activeScreen.runAction(EventHandler.RESTART, null)
    }

    override fun execute(data: String): String {
        utils.installRegisterScript()
        utils.installUnregisterScript()
        utils.installCommandScript()
        utils.installNormalScript()
        //utils.getActiveScreen().runAction(EventHandler.RESTART, null);
        Toast.makeText(utils.lightningContext, utils.getString(R.string.toast_done), Toast.LENGTH_SHORT).show()
        return ""
    }

    companion object {
        internal const val ITEM_ALBUM_ART = "albumart"
        internal const val VARIABLE_TITLE = "title"
        internal const val VARIABLE_ALBUM = "album"
        internal const val VARIABLE_ARTIST = "artist"
        internal const val VARIABLE_PLAYER = "player"
        internal const val PLAY = 5
        internal const val NEXT = 6
        internal const val PREVIOUS = 7
    }
}
