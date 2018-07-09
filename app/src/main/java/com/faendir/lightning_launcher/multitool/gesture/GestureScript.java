package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Intent;
import android.support.annotation.Keep;
import com.faendir.lightning_launcher.multitool.MainActivity;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.CustomView;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.Menu;
import com.faendir.lightning_launcher.multitool.proxy.PropertyEditor;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;
import com.faendir.lightning_launcher.multitool.proxy.Screen;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import com.faendir.lightning_launcher.multitool.util.FragmentManager;

/**
 * @author lukas
 * @since 09.07.18
 */
@Keep
public class GestureScript implements ProxyFactory.MenuScript {
    private final Utils utils;

    public GestureScript(Utils utils) {
        this.utils = utils;
    }

    @Override
    public void showMenu(Menu menu, Item item) {
        int mode = menu.getMode();
        if (mode == Menu.MODE_ITEM_NO_EM || mode == Menu.MODE_ITEM_EM) {
            utils.addMenuMainItem(menu, "Edit Gestures", () -> {
                Intent intent = new Intent(utils.getMultitoolContext(), MainActivity.class);
                intent.putExtra(FragmentManager.EXTRA_MODE, R.string.title_gestureLauncher);
                utils.getLightningContext().startActivity(intent);
                menu.close();
            });
        }
    }

    public void setup() {
        Screen screen = utils.getActiveScreen();
        CustomView view = utils.getContainer().addCustomView(screen.getLastTouchX(), screen.getLastTouchY());
        view.setHorizontalGrab(true);
        view.setVerticalGrab(true);
        Script script = utils.installScript("gesture", R.raw.gesture, "Gesture Launcher");
        Script menu = utils.installScript("gesture", R.raw.gesture_menu, "Menu");
        PropertyEditor editor = view.getProperties()
                .edit()
                .setString("v.onCreate", "" + script.getId())
                .setString("i.selectionEffect", "PLAIN")
                .setEventHandler("i.menu", EventHandler.RUN_SCRIPT, String.valueOf(menu.getId()));
        editor.getBox("i.box").setColor("c", "nsf", 0x42FfFfFf);
        editor.commit();
    }
}
