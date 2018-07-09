package com.faendir.lightning_launcher.multitool.drawer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Keep;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.Folder;
import com.faendir.lightning_launcher.multitool.proxy.ImageBitmap;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.Menu;
import com.faendir.lightning_launcher.multitool.proxy.Panel;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;
import com.faendir.lightning_launcher.multitool.proxy.RectL;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import java9.util.Comparators;
import java9.util.Lists;
import java9.util.stream.Collectors;
import java9.util.stream.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author lukas
 * @since 08.07.18
 */
@Keep
public class Drawer implements ProxyFactory.MenuScript {
    private final Utils utils;
    private final PackageManager pm;

    public Drawer(Utils utils) {
        this.utils = utils;
        pm = utils.getLightningContext().getPackageManager();
    }

    @Override
    public void showMenu(Menu menu, Item item) {
        int mode = menu.getMode();
        if (mode == Menu.MODE_ITEM_NO_EM || mode == Menu.MODE_ITEM_EM) {
            utils.addMenuMainItem(menu, "Hide", () -> this.hide(menu, item));
        }
    }

    private void hide(Menu menu, Item item) {
        menu.close();
        String name = item.getTag("intent");
        if (name != null) {
            Set<String> hidden = utils.getSharedPref().getStringSet(utils.getString(R.string.pref_hiddenApps), new HashSet<>());
            if (!hidden.contains(name)) {
                hidden.add(name);
                utils.getSharedPref().edit().putStringSet(utils.getString(R.string.pref_hiddenApps), hidden).apply();
                item.getParent().removeItem(item);
                utils.getActiveScreen().runScript("com/faendir/lightning_launcher/multitool/drawer", "AppDrawer", null);
            }
        }
    }

    public void update() {
        List<ComponentName> old = getPresentActivities();
        List<ResolveInfo> current = getCurrentActivities();
        SharedPreferences prefs = utils.getSharedPref();
        Set<String> hidden = prefs.getStringSet(utils.getString(R.string.pref_hiddenApps), Collections.emptySet());
        for (ResolveInfo app : current) {
            ActivityInfo activity = app.activityInfo;
            ComponentName name = new ComponentName(activity.packageName, activity.name);
            if (hidden.contains(name.flattenToString())) {
                continue;
            }
            if (old.contains(name)) {
                old.remove(name);
                continue;
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName(activity.packageName, activity.name);
            Shortcut item = utils.getContainer().addShortcut(String.valueOf(app.loadLabel(pm)), intent, 0, 0);
            item.setTag("intent", name.flattenToString());
            Bitmap bmp = toBitmap(app.loadIcon(pm));
            ImageBitmap img = utils.getImageClass().createImage(bmp.getWidth(), bmp.getHeight());
            img.draw().drawBitmap(bmp, 0, 0, null);
            item.setDefaultIcon(img);
        }
        for (ComponentName name : old) {
            String flat = name.flattenToString();
            getItemsDeep(utils.getContainer()).forEach(item -> {
                String tag = item.getTag("intent");
                if (Objects.equals(tag, flat)) {
                    item.getParent().removeItem(item);
                }
            });
        }
        if (prefs.getBoolean(utils.getString(R.string.pref_keepSorted), true)) {
            new Handler(utils.getLightningContext().getMainLooper()).post(() -> deepSort(utils.getContainer()));
        }
    }

    private List<ComponentName> getPresentActivities() {
        return getItemsDeep(utils.getContainer()).map(item -> item.getTag("intent"))
                .filter(java9.util.Objects::nonNull)
                .map(ComponentName::unflattenFromString)
                .collect(Collectors.toList());
    }

    private List<ResolveInfo> getCurrentActivities() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return pm.queryIntentActivities(intent, 0);
    }

    private Stream<Shortcut> getItemsDeep(Container container) {
        return Stream.of(container.getAllItems()).flatMap(item -> {
            switch (item.getType()) {
                case "Panel":
                    try {
                        return getItemsDeep(ProxyFactory.cast(item, Panel.class).getContainer());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                case "Folder":
                    return getItemsDeep(ProxyFactory.cast(item, Folder.class).getContainer());
                case "Shortcut":
                    return Stream.of(ProxyFactory.cast(item, Shortcut.class));
            }
            return Stream.empty();
        });
    }

    private Bitmap toBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    private void deepSort(Container container) {
        Item[] items = container.getAllItems();
        Matrix matrix = new Matrix();
        List<Shortcut> move = new ArrayList<>();
        for (Item item : items) {
            String type = item.getType();
            switch (type) {
                case "Panel":
                    deepSort(ProxyFactory.cast(item, Panel.class).getContainer());
                    break;
                case "Folder":
                    deepSort(ProxyFactory.cast(item, Folder.class).getContainer());
                    break;
                case "Shortcut":
                    String tag = item.getTag("intent");
                    if (tag != null) move.add(ProxyFactory.cast(item, Shortcut.class));
                    break;
            }
            //noinspection SuspiciousMethodCalls
            if (!move.contains(item) && item.getProperties().getBoolean("i.onGrid")) {
                RectL cell = item.getCell();
                for (int x = cell.getLeft(); x < cell.getRight(); x++) {
                    for (int y = cell.getTop(); y < cell.getBottom(); y++) {
                        matrix.mark(x, y);
                    }
                }
            }
        }
        Lists.sort(move, Comparators.comparing(Shortcut::getLabel));
        int width = Math.round(container.getWidth() / container.getCellWidth());
        int x = 0;
        int y = 0;
        for (Shortcut item : move) {
            while (matrix.get(x, y)) {
                if (++x >= width) {
                    x = 0;
                    y++;
                }
            }
            matrix.mark(x, y);
            item.setCell(x, y, x + 1, y + 1, true);
        }
    }

    private static class Matrix {
        private final List<List<Boolean>> list;

        private Matrix() {
            list = new ArrayList<>();
        }

        void mark(int x, int y) {
            while (y >= list.size()) {
                list.add(new ArrayList<>());
            }
            List<Boolean> row = list.get(y);
            while (x >= row.size()) {
                row.add(false);
            }
            row.set(x, true);
        }

        boolean get(int x, int y) {
            if (y < list.size()) {
                List<Boolean> row = list.get(y);
                if (x < row.size()) {
                    return row.get(x);
                }
            }
            return false;
        }
    }
}
