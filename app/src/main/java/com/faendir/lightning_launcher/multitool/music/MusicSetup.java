package com.faendir.lightning_launcher.multitool.music;

import android.content.Intent;
import com.faendir.lightning_launcher.multitool.BuildConfig;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.Lightning;
import com.faendir.lightning_launcher.multitool.proxy.Panel;
import com.faendir.lightning_launcher.multitool.proxy.PropertyEditor;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

/**
 * @author lukas
 * @since 05.07.18
 */
public class MusicSetup {
    private final Utils utils;

    public MusicSetup(Lightning lightning) {
        utils = new Utils(lightning);
    }

    public void create() {
        Script resume = utils.installScript("music", R.raw.music_resume, "resume");
        Script pause = utils.installScript("music", R.raw.music_pause, "pause");
        Script command = utils.installScript("music", R.raw.music_command, "command");
        Script launch = utils.installScript("music", R.raw.music_launch, "launch");
        int size = 500;
        Container container = utils.getContainer();
        Panel panel = container.addPanel(0, 0, size, size);
        PropertyEditor panelEditor = panel.getProperties().edit();
        panelEditor.setBoolean("i.onGrid", false);
        panelEditor.getBox("i.box").setColor("bl,br,bt,bb", "nfs", 0x00000000);
        panelEditor.commit();
        panel.setSize(size, size);
        Container p = panel.getContainer();
        p.getProperties()
                .edit()
                .setEventHandler("resumed", EventHandler.RUN_SCRIPT, String.valueOf(resume.getId()))
                .setEventHandler("paused", EventHandler.RUN_SCRIPT, String.valueOf(pause.getId()))
                .setString("scrollingDirection", "NONE")
                .setInteger("gridPColumnNum", 3)
                .setInteger("gridPRowNum", 10)
                .setInteger("gridLColumnNum", 3)
                .setInteger("gridLRowNum", 10)
                .commit();
        Shortcut albumart = p.addShortcut("albumart", new Intent(), 0, 0);
        albumart.setName("albumart");
        Shortcut title = p.addShortcut("title", new Intent(), 0, 0);
        Shortcut album = p.addShortcut("album", new Intent(), 0, 0);
        Shortcut artist = p.addShortcut("artist", new Intent(), 0, 0);
        Shortcut play = p.addShortcut("Play/Pause", new Intent(), 0, 0);
        Shortcut next = p.addShortcut("Next", new Intent(), 0, 0);
        Shortcut previous = p.addShortcut("Previous", new Intent(), 0, 0);
        PropertyEditor albumartEditor = albumart.getProperties().edit();
        albumartEditor.setBoolean("s.labelVisibility", false)
                .setBoolean("s.iconVisibility", false)
                .setEventHandler("i.tap", EventHandler.RUN_SCRIPT, String.valueOf(launch.getId()));
        albumartEditor.getBox("i.box").setColor("c", "s", 0xffffffff);
        albumartEditor.commit();
        title.getProperties().edit().setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).commit();
        album.getProperties().edit().setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).commit();
        artist.getProperties().edit().setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).commit();
        play.getProperties().edit().setBoolean("s.labelVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, command.getId() + "/5").commit();
        play.setDefaultIcon(utils.getImageClass().createImage(BuildConfig.APPLICATION_ID, utils.getMultitoolResources().getResourceName(R.drawable.ic_play)));
        next.getProperties().edit().setBoolean("s.labelVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, command.getId() + "/6").commit();
        next.setDefaultIcon(utils.getImageClass().createImage(BuildConfig.APPLICATION_ID, utils.getMultitoolResources().getResourceName(R.drawable.ic_next)));
        previous.getProperties().edit().setBoolean("s.labelVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, command.getId() + "/7").commit();
        previous.setDefaultIcon(utils.getImageClass().createImage(BuildConfig.APPLICATION_ID, utils.getMultitoolResources().getResourceName(R.drawable.ic_previous)));
        title.setBinding("s.label", "$title", true);
        album.setBinding("s.label", "$album", true);
        artist.setBinding("s.label", "$artist", true);
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
}
