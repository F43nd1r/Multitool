package com.faendir.lightning_launcher.multitool.gesture;

import android.content.Intent;
import android.support.annotation.Keep;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.faendir.lightning_launcher.multitool.MainActivity;
import com.faendir.lightning_launcher.multitool.MultiTool;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.Box;
import com.faendir.lightning_launcher.multitool.proxy.CustomView;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.Menu;
import com.faendir.lightning_launcher.multitool.proxy.PropertyEditor;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;
import com.faendir.lightning_launcher.multitool.proxy.Screen;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import com.faendir.lightning_launcher.multitool.util.FragmentManager;

/**
 * @author lukas
 * @since 09.07.18
 */
@Keep
public class GestureScript implements JavaScript.CreateMenu, JavaScript.Setup, JavaScript.CreateCustomView {
    private final Utils utils;

    public GestureScript(Utils utils) {
        this.utils = utils;
    }

    @Override
    public void showMenu(Menu menu, Item item) {
        int mode = menu.getMode();
        if (mode == Menu.MODE_ITEM_NO_EM || mode == Menu.MODE_ITEM_EM) {
            menu.addMainItem(utils.getString(R.string.menu_editGestures), utils.asFunction(() -> {
                Intent intent = new Intent(utils.getMultitoolContext(), MainActivity.class);
                intent.putExtra(FragmentManager.EXTRA_MODE, R.string.title_gestureLauncher);
                utils.getLightningContext().startActivity(intent);
                menu.close();
            }));
        }
    }

    @Override
    public void setup() {
        Screen screen = utils.getActiveScreen();
        CustomView view = utils.getContainer().addCustomView(screen.getLastTouchX(), screen.getLastTouchY());
        view.setHorizontalGrab(true);
        view.setVerticalGrab(true);
        Script script = utils.installCreateViewScript();
        Script menu = utils.installMenuScript();
        PropertyEditor editor = view.getProperties()
                .edit()
                .setString(PropertySet.VIEW_ON_CREATE, script.getId() + "/" + getClass().getName())
                .setString(PropertySet.ITEM_SELECTION_EFFECT, PropertySet.ITEM_SELECTION_EFFECT_PLAIN)
                .setEventHandler(PropertySet.ITEM_MENU, EventHandler.RUN_SCRIPT, menu.getId() + "/" + getClass().getName());
        editor.getBox(PropertySet.ITEM_BOX).setColor(Box.CONTENT, Box.MODE_ALL, 0x42FfFfFf);
        editor.commit();
    }

    @Override
    public View onCreate(CustomView item) {
        item.setHorizontalGrab(true);
        item.setVerticalGrab(true);

        try{
            return new LightningGestureView(utils.getLightningContext());
        } catch (Exception e) {
            Log.w(MultiTool.LOG_TAG, "Failed to load gesture widget");
            TextView t = new TextView(utils.getLightningContext());
            t.setText(utils.getString(R.string.text_gestureViewFailed));
            return t;
        }
    }
}
