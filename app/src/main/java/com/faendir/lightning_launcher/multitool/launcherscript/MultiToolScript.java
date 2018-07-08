package com.faendir.lightning_launcher.multitool.launcherscript;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.faendir.lightning_launcher.multitool.BuildConfig;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem;
import com.faendir.lightning_launcher.multitool.fastadapter.ItemFactory;
import com.faendir.lightning_launcher.multitool.fastadapter.Model;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.Desktop;
import com.faendir.lightning_launcher.multitool.proxy.Event;
import com.faendir.lightning_launcher.multitool.proxy.Image;
import com.faendir.lightning_launcher.multitool.proxy.ImageBitmap;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.Lightning;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.mikepenz.fastadapter.expandable.ExpandableExtension;
import com.mikepenz.fastadapter.utils.DefaultItemListImpl;
import java9.util.function.Consumer;
import java9.util.stream.Collectors;
import java9.util.stream.Stream;
import java9.util.stream.StreamSupport;
import org.acra.util.StreamReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author lukas
 * @since 04.07.18
 */
public class MultiToolScript {
    private final Context context;
    private final Lightning lightning;
    private final Context packageContext;
    private final Event event;

    public MultiToolScript(Lightning lightning) {
        this.lightning = lightning;
        this.context = lightning.getActiveScreen().getContext();
        this.event = lightning.getEvent();
        try {
            this.packageContext = context.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void show() {
        RecyclerView recyclerView = new RecyclerView(packageContext);
        recyclerView.setLayoutManager(new LinearLayoutManager(packageContext));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(recyclerView)
                .setCancelable(true)
                .setTitle("What do you want to do?")
                .setNegativeButton(packageContext.getString(R.string.button_cancel), (d, which) -> d.cancel())
                .create();
        ItemFactory<Model> factory = ItemFactory.forLauncherIconSize(context);
        boolean hasItem = event.getItem() != null;
        ExpandableItem<Model> information = factory.wrap(new ActionGroup("Information"));
        Stream<Action> informations = Stream.of(new Action("Event", () -> showEventInfo(dialog)), new Action("Container", () -> showContainerInfo(dialog)));
        if (hasItem) {
            informations = Stream.concat(informations,
                    Stream.of(new Action("Item", () -> showItemInfo(dialog)), new Action("Intent", () -> showIntentInfo(dialog)), new Action("Icon", () -> showIconInfo(dialog))));
        }
        information.withSubItems(informations.map(factory::wrap).collect(Collectors.toList()));
        ExpandableItem<Model> itemUtils = factory.wrap(new ActionGroup("Item Utilities"));
        itemUtils.withSubItems(Stream.of(new Action("Attach/Detach all items", () -> showAttachDetach(dialog)),
                new Action("Resize all detached items", () -> showResizeDetached(dialog)),
                new Action("Delete all items", () -> showDelete(dialog))).map(factory::wrap).collect(Collectors.toList()));
        ExpandableItem<Model> other = factory.wrap(new ActionGroup("Other"));
        other.withSubItems(Stream.of(new Action("Reset Tag", () -> showResetTag(dialog)),
                new Action("Reset Tool", () -> showResetTool(dialog)),
                new Action("Save changes", () -> save(dialog)),
                new Action("Delete recent app history", () -> deleteHistory(dialog))).map(factory::wrap).collect(Collectors.toList()));
        FastAdapter fastAdapter = FastAdapter.with(new ModelAdapter<>(new DefaultItemListImpl<>(new ArrayList<>(Arrays.asList(information, itemUtils, other))), factory::wrap))
                .addExtension(new ExpandableExtension<>());
        recyclerView.setAdapter(fastAdapter);
        dialog.show();
    }

    private void deleteHistory(AlertDialog dialog) {
        dialog.dismiss();
        //noinspection ResultOfMethodCallIgnored
        new File(context.getFilesDir().getPath() + "/statistics").delete();
        Toast.makeText(context, "Recents deleted", Toast.LENGTH_SHORT).show();
    }

    private void save(AlertDialog dialog) {
        dialog.dismiss();
        lightning.save();
        Toast.makeText(context, "Saved Layout", Toast.LENGTH_SHORT).show();
    }

    private void showResetTool(AlertDialog dialog) {
        dialog.dismiss();
        Container cont = event.getContainer();
        Item[] items = cont.getAllItems();
        CharSequence[] listItems = new CharSequence[]{"Cell (only grid items) [0,0]",
                                                      "Position (only free items) [0,0]",
                                                      "Rotation (only free items) [0]",
                                                      "Scale (only free items) [1,1]",
                                                      "Skew (only free items) [0,0]",
                                                      "Size (only free items) [cell size]",
                                                      "Visibility [true]"};
        boolean[] bools = new boolean[listItems.length];
        new AlertDialog.Builder(context).setMultiChoiceItems(listItems, null, (dialog1, which, isChecked) -> bools[which] = isChecked)
                .setTitle("Reset")
                .setCancelable(true)
                .setPositiveButton(packageContext.getString(R.string.button_confirm), (dialog1, which) -> {
                    for (Item item : items) {
                        if (bools[0]) item.setCell(0, 0, 1, 1);
                        if (bools[1]) item.setPosition(0, 0);
                        if (bools[2]) item.setRotation(0);
                        if (bools[3]) item.setScale(1, 1);
                        if (bools[4]) item.setSkew(0, 0);
                        if (bools[5]) item.setSize(cont.getCellWidth(), cont.getCellHeight());
                        if (bools[6]) item.setVisibility(true);
                    }
                })
                .show();
    }

    private void showResetTag(AlertDialog dialog) {
        dialog.dismiss();
        Container container = event.getContainer();
        Item item1 = event.getItem();
        Set<String> tags;
        Consumer<String> deleter;
        if (item1 != null) { //Items Tag
            tags = getTags(item1).keySet();
            deleter = tag -> item1.setTag(tag, null);
        } else {
            tags = getTags(container).keySet();
            deleter = tag -> container.setTag(tag, null);
        }
        List<String> options = new ArrayList<>(tags);
        options.add(0, "All tags");
        new AlertDialog.Builder(context).setTitle("Which tag do you want to reset?").setCancelable(true).setItems(options.toArray(new CharSequence[0]), (dialog1, which) -> {
            List<String> delete = which == 0 ? new ArrayList<>(tags) : Collections.singletonList(options.get(which));
            for (String tag : delete) {
                deleter.accept(tag);
            }
            Toast.makeText(context, "Deleting tag(s) done!", Toast.LENGTH_SHORT).show();
            lightning.save();
        }).setNegativeButton(packageContext.getString(R.string.button_cancel), null).show();
    }

    private void showDelete(AlertDialog dialog) {
        dialog.dismiss();
        new AlertDialog.Builder(context).setTitle("Delete all items")
                .setMessage("Are you sure?")
                .setPositiveButton(packageContext.getString(R.string.button_confirm), (dialog1, which) -> {
                    Container container = event.getContainer();
                    for (Item item : container.getAllItems()) {
                        container.removeItem(item);
                    }
                })
                .setNegativeButton(packageContext.getString(R.string.button_cancel), null)
                .show();
    }

    private void showResizeDetached(AlertDialog dialog) {
        dialog.dismiss();
        LinearLayout linearLayout = new LinearLayout(context);
        Container c = event.getContainer();
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView widthText = new TextView(context);
        widthText.setText("Width: ");
        linearLayout.addView(widthText);
        NumberPicker widthPicker = new NumberPicker(context);
        widthPicker.setMinValue(1);
        widthPicker.setMaxValue(9999);
        widthPicker.setValue((int) c.getCellWidth());
        linearLayout.addView(widthPicker);
        TextView heightText = new TextView(context);
        heightText.setText("Height: ");
        linearLayout.addView(heightText);
        NumberPicker heightPicker = new NumberPicker(context);
        heightPicker.setMinValue(1);
        heightPicker.setMaxValue(9999);
        heightPicker.setValue((int) c.getCellHeight());
        linearLayout.addView(heightPicker);
        new AlertDialog.Builder(context).setView(linearLayout)
                .setCancelable(true)
                .setTitle("To which size?")
                .setPositiveButton(packageContext.getString(R.string.button_confirm), (dialog1, which) -> {
                    int width = widthPicker.getValue();
                    int height = heightPicker.getValue();
                    Item[] items = c.getAllItems();
                    for (Item item : items) {
                        item.setSize(width, height);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAttachDetach(AlertDialog dialog) {
        dialog.dismiss();
        new AlertDialog.Builder(context).setTitle("MultiTool")
                .setMessage("Do you want to attach or detach all items?")
                .setCancelable(true)
                .setPositiveButton("Attach", (dialog1, which) -> attachDetach(true))
                .setNegativeButton("Detach", (dialog1, which) -> attachDetach(false))
                .setNeutralButton(packageContext.getString(R.string.button_cancel), null)
                .show();
    }

    private void attachDetach(boolean attach) {
        Item[] items = event.getContainer().getAllItems();
        for (Item i : items) {
            i.getProperties().edit().setBoolean("i.onGrid", attach).commit();
        }
        Toast.makeText(context, "Done!", Toast.LENGTH_SHORT).show();
    }

    private void showIconInfo(AlertDialog dialog) {
        dialog.dismiss();
        Item it = event.getItem();
        //create view structure
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        //check for all kinds of images in this item and add them to the view if there are any
        addImageIfNotNull(root, it.getBoxBackground("n"), "Normal Box Background");
        addImageIfNotNull(root, it.getBoxBackground("s"), "Selected Box Background");
        addImageIfNotNull(root, it.getBoxBackground("f"), "Focused Box Background");
        if ("Shortcut".equals(it.getType())) {
            Shortcut shortcut = ProxyFactory.cast(it, Shortcut.class);
            addImageIfNotNull(root, shortcut.getDefaultIcon(), "Default Icon");
            addImageIfNotNull(root, shortcut.getCustomIcon(), "Custom Icon");
        }
        if (root.getChildCount() <= 0) {
            Toast.makeText(context, "No Image Data available", Toast.LENGTH_SHORT).show(); //no image found
            return;
        }
        //at least one image found
        ScrollView scroll = new ScrollView(context);
        scroll.addView(root);
        new AlertDialog.Builder(context).setView(scroll)
                .setCancelable(true)
                .setTitle("Icon")
                .setNeutralButton(packageContext.getString(R.string.button_close), (d, which) -> d.dismiss())
                .show();
    }

    private void addImageIfNotNull(LinearLayout root, Image image, String txt) {
        if (image != null) {
            TextView textView = new TextView(context);
            textView.setText(String.format(Locale.US, "%s (%dx%d)", txt, image.getWidth(), image.getHeight()));
            root.addView(textView);
            if ("BITMAP".equals(image.getType())) {
                ImageView imageView = new ImageView(context);
                imageView.setImageBitmap(ProxyFactory.cast(image, ImageBitmap.class).getBitmap());
                root.addView(imageView);
            }
        }
    }

    private void showIntentInfo(AlertDialog dialog) {
        dialog.dismiss();
        Item it = event.getItem();
        if (it == null || !"Shortcut".equals(it.getType())) {
            Toast.makeText(context, "No Intent found", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = ProxyFactory.cast(it, Shortcut.class).getIntent();
        intent.getStringExtra("somenamenoonewouldeveruse");
        showText("Intent: " + intent + "\nExtras: " + intent.getExtras(), "Intent Information");
    }

    private void showItemInfo(AlertDialog dialog) {
        dialog.dismiss();
        Item i = event.getItem();
        if (i == null) {
            Toast.makeText(context, "no item found", Toast.LENGTH_SHORT).show();
            return;
        }

        String tags = StreamSupport.stream(getTags(i).entrySet()).map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining("\n"));
        String label;
        switch (i.getType()) {
            case "Shortcut":
            case "Folder":
                label = ProxyFactory.cast(i, Shortcut.class).getLabel();
                break;
            default:
                label = "";
                break;
        }
        showText("Label: " + label + "\nName: " + i.getName() + "\nType: " + i.getType() + "\nID: " + i.getId() + "\nSize: " + i.getWidth() + "," + i.getHeight() + "\nPosition: "
                 + i.getPositionX() + "," + i.getPositionY() + "\nScale: " + i.getScaleX() + "," + i.getScaleY() + "\nAngle: " + i.getRotation() + "\nCenter: " + center(i)
                 + "\nCell: " + i.getCell() + "\nis visible: " + i.isVisible() + "\nTags: " + tags, "Item Information");
    }

    private Map<String, String> getTags(Item item) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            String s = new StreamReader(context.getFilesDir().getPath() + "/pages/" + item.getParent().getId() + "/items").read();
            JSONArray all = new JSONObject(s).getJSONArray("i");
            int x;
            JSONObject jsonItem = new JSONObject();
            for (x = 0; x < all.length(); x++) {
                jsonItem = all.getJSONObject(x);
                if (jsonItem.getInt("b") == item.getId()) break;
            }
            if (x != all.length()) {
                JSONObject jsonTags = jsonItem.getJSONObject("an");
                for (Iterator<String> iterator = jsonTags.keys(); iterator.hasNext(); ) {
                    String property = iterator.next();
                    result.put(property, jsonTags.getString(property));
                }
            } else {
                Toast.makeText(context, "Can't find Tags", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Map<String, String> getTags(Container container) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("_", container.getTag());
        try {
            String s = new StreamReader(context.getFilesDir().getPath() + "/pages/" + container.getId() + "/conf").read();
            JSONObject data = new JSONObject(s);
            if (data.has("tags")) {
                JSONObject jsonTags = data.getJSONObject("tags");
                for (Iterator<String> iterator = jsonTags.keys(); iterator.hasNext(); ) {
                    String property = iterator.next();
                    result.put(property, jsonTags.getString(property));
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void showContainerInfo(AlertDialog dialog) {
        dialog.dismiss();
        Container c = event.getContainer();
        String t = c.getType(); //Differentiate between Desktop and other containers

        //read Tags from launcher file
        StringBuilder tags = new StringBuilder("Default: " + c.getTag());
        try {
            String s = new StreamReader(context.getFilesDir().getPath() + "/pages/" + c.getId() + "/conf").read();
            JSONObject data = new JSONObject(s);
            if (data.has("tags")) {
                JSONObject jsonTags = data.getJSONObject("tags");
                for (Iterator<String> iterator = jsonTags.keys(); iterator.hasNext(); ) {
                    String property = iterator.next();
                    tags.append("\n").append(property).append(": ").append(jsonTags.get(property));
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        String name;
        switch (t) {
            case "Desktop":
                name = ProxyFactory.cast(c, Desktop.class).getName();
                break;
            case "Folder":
                name = ProxyFactory.cast(c.getOpener(), Shortcut.class).getLabel();
                break;
            default:
                name = c.getOpener().getName();
                break;
        }
        showText("Type: " + t + "\nName/Label: " + name + "\nID: " + c.getId() + "\nSize: " + c.getWidth() + "," + c.getHeight() + "\nBoundingbox: " + c.getBoundingBox()
                 + "\nCell Size: " + c.getCellWidth() + "," + c.getCellHeight() + "\nCurrent Position: " + c.getPositionX() + "," + c.getPositionY() + "\nCurrent Scale: "
                 + c.getPositionScale() + "\nTags: " + tags + "\nItems: " + Arrays.toString(c.getAllItems()), "Container Information");
    }

    private void showEventInfo(AlertDialog dialog) {
        dialog.dismiss();
        Event event = this.event;
        boolean ok;
        try { //test if event contains touch data
            event.getTouchScreenX();
            ok = true;
        } catch (Exception e) {
            ok = false;
        }
        showText("Source: " + event.getSource() + "\nDate: " + DateFormat.getInstance().format(event.getDate()) + "\nContainer: " + event.getContainer() + "\nScreen: "
                 + event.getScreen() + "\nItem: " + event.getItem() + "\nData: " + event.getData() + (ok ?
                                                                                                              ("\nTouch: " + event.getTouchX() + "," + event.getTouchY()
                                                                                                               + "\nTouch (Screen): " + event.getTouchScreenX() + ","
                                                                                                               + event.getTouchScreenY()) :
                                                                                                              ""), "Event Information");
    }

    private void showText(String text, String title) {
        new AlertDialog.Builder(context).setTitle(title)
                .setMessage(text)
                .setCancelable(true)
                .setNeutralButton(packageContext.getString(R.string.button_close), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String center(Item item) {
        double r = item.getRotation() * Math.PI / 180;
        double sin = Math.abs(Math.sin(r));
        double cos = Math.abs(Math.cos(r));
        double w = item.getWidth() * item.getScaleX();
        double h = item.getHeight() * item.getScaleY();
        return (item.getPositionX() + (w * cos + h * sin) * 0.5) + "," + (item.getPositionY() + (h * cos + w * sin) * 0.5);
    }
}
