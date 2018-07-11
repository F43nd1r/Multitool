package com.faendir.lightning_launcher.multitool.music;

import android.content.Intent;
import android.support.annotation.Keep;
import android.widget.Toast;
import com.faendir.lightning_launcher.multitool.BuildConfig;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.Box;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.Panel;
import com.faendir.lightning_launcher.multitool.proxy.PropertyEditor;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

/**
 * @author lukas
 * @since 05.07.18
 */
@Keep
public class MusicSetup implements JavaScript.Setup, JavaScript.Direct {
    static final String ITEM_ABUM_ART = "albumart";
    static final String VARIABLE_TITLE = "title";
    static final String VARIABLE_ALBUM = "album";
    static final String VARIABLE_ARTIST = "artist";
    static final String VARIABLE_PLAYER = "player";
    static final int PLAY = 5;
    static final int NEXT = 6;
    static final int PREVIOUS = 7;
    private final Utils utils;

    public MusicSetup(Utils utils) {
        this.utils = utils;
    }

    @Override
    public void setup() {
        Script resume = utils.installRegisterScript();
        Script pause = utils.installUnregisterScript();
        Script command = utils.installCommandScript();
        Script launch = utils.installNormalScript();
        int size = 500;
        Container container = utils.getContainer();
        Panel panel = container.addPanel(0, 0, size, size);
        PropertyEditor panelEditor = panel.getProperties().edit();
        panelEditor.setBoolean(PropertySet.ITEM_ON_GRID, false);
        panelEditor.getBox(PropertySet.ITEM_BOX).setColor(Box.border(), Box.MODE_ALL, 0x00000000);
        panelEditor.commit();
        panel.setSize(size, size);
        Container p = panel.getContainer();
        p.getProperties()
                .edit()
                .setEventHandler(PropertySet.RESUMED, EventHandler.RUN_SCRIPT, resume.getId() + "/" + MusicListener.LightningMusicListener.class.getName())
                .setEventHandler(PropertySet.PAUSED, EventHandler.RUN_SCRIPT, String.valueOf(pause.getId()))
                .setString(PropertySet.SCROLLING_DIRECTION, PropertySet.SCROLLING_DIRECTION_NONE)
                .setInteger(PropertySet.GRID_PORTRAIT_COLUMN_NUM, 3)
                .setInteger(PropertySet.GRID_PORTRAIT_ROW_NUM, 10)
                .setInteger(PropertySet.GRID_LANDSCAPE_COLUMN_NUM, 3)
                .setInteger(PropertySet.GRID_LANDSCAPE_ROW_NUM, 10)
                .commit();
        Shortcut albumart = p.addShortcut("albumart", new Intent(), 0, 0);
        albumart.setName(ITEM_ABUM_ART);
        Shortcut title = p.addShortcut("title", new Intent(), 0, 0);
        Shortcut album = p.addShortcut("album", new Intent(), 0, 0);
        Shortcut artist = p.addShortcut("artist", new Intent(), 0, 0);
        Shortcut play = p.addShortcut("Play/Pause", new Intent(), 0, 0);
        Shortcut next = p.addShortcut("Next", new Intent(), 0, 0);
        Shortcut previous = p.addShortcut("Previous", new Intent(), 0, 0);
        PropertyEditor albumartEditor = albumart.getProperties().edit();
        albumartEditor.setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, false)
                .setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false)
                .setEventHandler(PropertySet.ITEM_TAP, EventHandler.RUN_SCRIPT, launch.getId() + "/" + MusicListener.LightningMusicListener.class.getName());
        albumartEditor.getBox(PropertySet.ITEM_BOX).setColor(Box.CONTENT, Box.MODE_SELECTED, 0xffffffff);
        albumartEditor.commit();
        title.getProperties().edit().setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false).setBoolean(PropertySet.ITEM_ENABLED, false).commit();
        album.getProperties().edit().setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false).setBoolean(PropertySet.ITEM_ENABLED, false).commit();
        artist.getProperties().edit().setBoolean(PropertySet.SHORTCUT_ICON_VISIBILITY, false).setBoolean(PropertySet.ITEM_ENABLED, false).commit();
        play.getProperties()
                .edit()
                .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, false)
                .setEventHandler(PropertySet.ITEM_TAP, EventHandler.RUN_SCRIPT, command.getId() + "/" + PLAY)
                .commit();
        play.setDefaultIcon(utils.getImageClass().createImage(BuildConfig.APPLICATION_ID, utils.getMultitoolResources().getResourceName(R.drawable.ic_play)));
        next.getProperties()
                .edit()
                .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, false)
                .setEventHandler(PropertySet.ITEM_TAP, EventHandler.RUN_SCRIPT, command.getId() + "/" + NEXT)
                .commit();
        next.setDefaultIcon(utils.getImageClass().createImage(BuildConfig.APPLICATION_ID, utils.getMultitoolResources().getResourceName(R.drawable.ic_next)));
        previous.getProperties()
                .edit()
                .setBoolean(PropertySet.SHORTCUT_LABEL_VISIBILITY, false)
                .setEventHandler(PropertySet.ITEM_TAP, EventHandler.RUN_SCRIPT, command.getId() + "/" + PREVIOUS)
                .commit();
        previous.setDefaultIcon(utils.getImageClass().createImage(BuildConfig.APPLICATION_ID, utils.getMultitoolResources().getResourceName(R.drawable.ic_previous)));
        title.setBinding(PropertySet.SHORTCUT_LABEL, '$' + VARIABLE_TITLE, true);
        album.setBinding(PropertySet.SHORTCUT_LABEL, '$' + VARIABLE_ALBUM, true);
        artist.setBinding(PropertySet.SHORTCUT_LABEL, '$' + VARIABLE_ARTIST, true);
        albumart.setCell(0, 0, 3, 10, true);
        title.setCell(0, 0, 3, 1, true);
        album.setCell(0, 1, 3, 2, true);
        artist.setCell(0, 2, 3, 3, true);
        play.setCell(1, 7, 2, 10, true);
        next.setCell(2, 7, 3, 10, true);
        previous.setCell(0, 7, 1, 10, true);
        utils.centerOnTouch(panel);
        utils.getLightning().getActiveScreen().runAction(EventHandler.RESTART, null);
    }

    @Override
    public String execute(String data) {
        utils.installRegisterScript();
        utils.installUnregisterScript();
        utils.installCommandScript();
        utils.installNormalScript();
        utils.getActiveScreen().runAction(EventHandler.RESTART, null);
        Toast.makeText(utils.getLightningContext(),utils.getString(R.string.toast_done),Toast.LENGTH_SHORT).show();
        return null;
    }
}
