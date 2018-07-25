package com.faendir.lightning_launcher.multitool.launcherscript;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Keep;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.animation.AnimationScript;
import com.faendir.lightning_launcher.multitool.fastadapter.ExpandableItem;
import com.faendir.lightning_launcher.multitool.fastadapter.ItemFactory;
import com.faendir.lightning_launcher.multitool.fastadapter.Model;
import com.faendir.lightning_launcher.multitool.proxy.Box;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.Desktop;
import com.faendir.lightning_launcher.multitool.proxy.Event;
import com.faendir.lightning_launcher.multitool.proxy.Image;
import com.faendir.lightning_launcher.multitool.proxy.ImageBitmap;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;
import com.faendir.lightning_launcher.multitool.proxy.Shortcut;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
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
import java.util.Map;
import java.util.Set;

/**
 * @author lukas
 * @since 04.07.18
 */
@Keep
public class MultiToolScript implements JavaScript.Normal {
    private final Utils utils;
    private final Event event;

    public MultiToolScript(Utils utils) {
        this.utils = utils;
        this.event = utils.getEvent();
    }

    @Override
    public void run() {
        RecyclerView recyclerView = new RecyclerView(utils.getMultitoolContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(utils.getMultitoolContext()));
        AlertDialog dialog = new AlertDialog.Builder(utils.getLightningContext()).setView(recyclerView)
                .setCancelable(true)
                .setTitle(utils.getString(R.string.title_multitoolScript))
                .setNegativeButton(utils.getString(R.string.button_cancel), (d, which) -> d.cancel())
                .create();
        ItemFactory<Model> factory = ItemFactory.forLauncherIconSize(utils.getLightningContext());
        boolean hasItem = event.getItem() != null;
        ExpandableItem<Model> information = factory.wrap(new ActionGroup(utils.getString(R.string.group_information)));
        Stream<Action> informations = Stream.of(new Action(utils.getString(R.string.action_event), () -> showEventInfo(dialog)),
                new Action(utils.getString(R.string.action_container), () -> showContainerInfo(dialog)));
        if (hasItem) {
            informations = Stream.concat(informations,
                    Stream.of(new Action(utils.getString(R.string.action_item), () -> showItemInfo(dialog)),
                            new Action(utils.getString(R.string.action_intent), () -> showIntentInfo(dialog)),
                            new Action(utils.getString(R.string.action_icon), () -> showIconInfo(dialog))));
        }
        information.withSubItems(informations.map(factory::wrap).collect(Collectors.toList()));
        ExpandableItem<Model> itemUtils = factory.wrap(new ActionGroup(utils.getString(R.string.group_utils)));
        itemUtils.withSubItems(Stream.of(new Action(utils.getString(R.string.action_attach), () -> showAttachDetach(dialog)),
                new Action(utils.getString(R.string.action_resize), () -> showResizeDetached(dialog)),
                new Action(utils.getString(R.string.action_delete), () -> showDelete(dialog))).map(factory::wrap).collect(Collectors.toList()));
        ExpandableItem<Model> other = factory.wrap(new ActionGroup(utils.getString(R.string.group_other)));
        other.withSubItems(Stream.of(new Action(utils.getString(R.string.action_resetTag), () -> showResetTag(dialog)),
                new Action(utils.getString(R.string.action_resetTool), () -> showResetTool(dialog)),
                new Action(utils.getString(R.string.action_save), () -> save(dialog)),
                new Action(utils.getString(R.string.action_deleteRecents), () -> deleteHistory(dialog))).map(factory::wrap).collect(Collectors.toList()));
        FastAdapter fastAdapter = FastAdapter.with(new ModelAdapter<>(new DefaultItemListImpl<>(new ArrayList<>(Arrays.asList(information, itemUtils, other))), factory::wrap))
                .addExtension(new ExpandableExtension<>());
        recyclerView.setAdapter(fastAdapter);
        dialog.show();
    }

    private void deleteHistory(AlertDialog dialog) {
        dialog.dismiss();
        //noinspection ResultOfMethodCallIgnored
        new File(utils.getLightningContext().getFilesDir().getPath() + "/statistics").delete();
        Toast.makeText(utils.getLightningContext(), utils.getString(R.string.toast_recentsDeleted), Toast.LENGTH_SHORT).show();
    }

    private void save(AlertDialog dialog) {
        dialog.dismiss();
        utils.getLightning().save();
        Toast.makeText(utils.getLightningContext(), utils.getString(R.string.toast_savedLayout), Toast.LENGTH_SHORT).show();
    }

    private void showResetTool(AlertDialog dialog) {
        dialog.dismiss();
        Container cont = event.getContainer();
        Item[] items = cont.getAllItems();
        CharSequence[] listItems = new CharSequence[]{utils.getString(R.string.tool_cell),
                                                      utils.getString(R.string.tool_position),
                                                      utils.getString(R.string.tool_rotation),
                                                      utils.getString(R.string.tool_scale),
                                                      utils.getString(R.string.tool_skew),
                                                      utils.getString(R.string.tool_size),
                                                      utils.getString(R.string.tool_visibility)};
        boolean[] bools = new boolean[listItems.length];
        new AlertDialog.Builder(utils.getLightningContext()).setMultiChoiceItems(listItems, null, (dialog1, which, isChecked) -> bools[which] = isChecked)
                .setTitle(utils.getString(R.string.title_reset))
                .setCancelable(true)
                .setPositiveButton(utils.getString(R.string.button_confirm), (dialog1, which) -> {
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
        options.add(0, utils.getString(R.string.text_allTags));
        new AlertDialog.Builder(utils.getLightningContext()).setTitle(utils.getString(R.string.title_tagChooser))
                .setCancelable(true)
                .setItems(options.toArray(new CharSequence[0]), (dialog1, which) -> {
                    List<String> delete = which == 0 ? new ArrayList<>(tags) : Collections.singletonList(options.get(which));
                    for (String tag : delete) {
                        deleter.accept(tag);
                    }
                    Toast.makeText(utils.getLightningContext(), utils.getString(R.string.toast_tagsDeleted), Toast.LENGTH_SHORT).show();
                    utils.getLightning().save();
                })
                .setNegativeButton(utils.getString(R.string.button_cancel), null)
                .show();
    }

    private void showDelete(AlertDialog dialog) {
        dialog.dismiss();
        new AlertDialog.Builder(utils.getLightningContext()).setTitle(utils.getString(R.string.title_deleteAll))
                .setMessage(utils.getString(R.string.text_areYouSure))
                .setPositiveButton(utils.getString(R.string.button_confirm), (dialog1, which) -> {
                    Container container = event.getContainer();
                    for (Item item : container.getAllItems()) {
                        container.removeItem(item);
                    }
                })
                .setNegativeButton(utils.getString(R.string.button_cancel), null)
                .show();
    }

    private void showResizeDetached(AlertDialog dialog) {
        dialog.dismiss();
        LinearLayout linearLayout = new LinearLayout(utils.getLightningContext());
        Container c = event.getContainer();
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView widthText = new TextView(utils.getLightningContext());
        widthText.setText(utils.getString(R.string.text_width));
        linearLayout.addView(widthText);
        NumberPicker widthPicker = new NumberPicker(utils.getLightningContext());
        widthPicker.setMinValue(1);
        widthPicker.setMaxValue(9999);
        widthPicker.setValue((int) c.getCellWidth());
        linearLayout.addView(widthPicker);
        TextView heightText = new TextView(utils.getLightningContext());
        heightText.setText(utils.getString(R.string.text_height));
        linearLayout.addView(heightText);
        NumberPicker heightPicker = new NumberPicker(utils.getLightningContext());
        heightPicker.setMinValue(1);
        heightPicker.setMaxValue(9999);
        heightPicker.setValue((int) c.getCellHeight());
        linearLayout.addView(heightPicker);
        new AlertDialog.Builder(utils.getLightningContext()).setView(linearLayout)
                .setCancelable(true)
                .setTitle(utils.getString(R.string.title_size))
                .setPositiveButton(utils.getString(R.string.button_confirm), (dialog1, which) -> {
                    int width = widthPicker.getValue();
                    int height = heightPicker.getValue();
                    Item[] items = c.getAllItems();
                    for (Item item : items) {
                        item.setSize(width, height);
                    }
                })
                .setNegativeButton(utils.getString(R.string.button_cancel), null)
                .show();
    }

    private void showAttachDetach(AlertDialog dialog) {
        dialog.dismiss();
        new AlertDialog.Builder(utils.getLightningContext()).setTitle(utils.getString(R.string.script_name))
                .setMessage(utils.getString(R.string.text_attach))
                .setCancelable(true)
                .setPositiveButton(utils.getString(R.string.button_attach), (dialog1, which) -> attachDetach(true))
                .setNegativeButton(utils.getString(R.string.button_detach), (dialog1, which) -> attachDetach(false))
                .setNeutralButton(utils.getString(R.string.button_cancel), null)
                .show();
    }

    private void attachDetach(boolean attach) {
        Item[] items = event.getContainer().getAllItems();
        for (Item i : items) {
            i.getProperties().edit().setBoolean(PropertySet.ITEM_ON_GRID, attach).commit();
        }
        Toast.makeText(utils.getLightningContext(), utils.getString(R.string.toast_done), Toast.LENGTH_SHORT).show();
    }

    private void showIconInfo(AlertDialog dialog) {
        dialog.dismiss();
        Item it = event.getItem();
        //create view structure
        LinearLayout root = new LinearLayout(utils.getLightningContext());
        root.setOrientation(LinearLayout.VERTICAL);

        //check for all kinds of images in this item and add them to the view if there are any
        addImageIfNotNull(root, it.getBoxBackground(Box.MODE_NORMAL), utils.getString(R.string.text_normalBox));
        addImageIfNotNull(root, it.getBoxBackground(Box.MODE_SELECTED), utils.getString(R.string.text_selectedBox));
        addImageIfNotNull(root, it.getBoxBackground(Box.MODE_FOCUSED), utils.getString(R.string.text_focusedBox));
        if (Item.TYPE_SHORTCUT.equals(it.getType())) {
            Shortcut shortcut = ProxyFactory.cast(it, Shortcut.class);
            addImageIfNotNull(root, shortcut.getDefaultIcon(), utils.getString(R.string.text_defaultIcon));
            addImageIfNotNull(root, shortcut.getCustomIcon(), utils.getString(R.string.text_customIcon));
        }
        if (root.getChildCount() <= 0) {
            Toast.makeText(utils.getLightningContext(), utils.getString(R.string.toast_noImages), Toast.LENGTH_SHORT).show(); //no image found
            return;
        }
        //at least one image found
        ScrollView scroll = new ScrollView(utils.getLightningContext());
        scroll.addView(root);
        new AlertDialog.Builder(utils.getLightningContext()).setView(scroll)
                .setCancelable(true)
                .setTitle(utils.getString(R.string.title_icon))
                .setNeutralButton(utils.getString(R.string.button_close), (d, which) -> d.dismiss())
                .show();
    }

    private void addImageIfNotNull(LinearLayout root, Image image, String txt) {
        if (image != null) {
            TextView textView = new TextView(utils.getLightningContext());
            textView.setText(utils.getString(R.string.text_imageInfo, txt, image.getWidth(), image.getHeight()));
            root.addView(textView);
            if (Image.TYPE_BITMAP.equals(image.getType())) {
                ImageView imageView = new ImageView(utils.getLightningContext());
                imageView.setImageBitmap(ProxyFactory.cast(image, ImageBitmap.class).getBitmap());
                root.addView(imageView);
            }
        }
    }

    private void showIntentInfo(AlertDialog dialog) {
        dialog.dismiss();
        Item it = event.getItem();
        if (it == null || !Item.TYPE_SHORTCUT.equals(it.getType())) {
            Toast.makeText(utils.getLightningContext(), utils.getString(R.string.toast_noIntent), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = ProxyFactory.cast(it, Shortcut.class).getIntent();
        intent.getStringExtra("somenamenoonewouldeveruse");
        showText(utils.getString(R.string.text_intentInfo, intent, intent.getExtras()), utils.getString(R.string.title_intentInfo));
    }

    private void showItemInfo(AlertDialog dialog) {
        dialog.dismiss();
        Item i = event.getItem();
        if (i == null) {
            Toast.makeText(utils.getLightningContext(), utils.getString(R.string.toast_noItem), Toast.LENGTH_SHORT).show();
            return;
        }

        String tags = StreamSupport.stream(getTags(i).entrySet()).map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining("\n"));
        String label;
        switch (i.getType()) {
            case Item.TYPE_SHORTCUT:
            case Item.TYPE_FOLDER:
                label = ProxyFactory.cast(i, Shortcut.class).getLabel();
                break;
            default:
                label = "";
                break;
        }
        showText(utils.getString(R.string.text_itemInfo,
                label,
                i.getName(),
                i.getType(),
                i.getId(),
                i.getWidth(),
                i.getHeight(),
                i.getPositionX(),
                i.getPositionY(),
                i.getScaleX(),
                i.getScaleY(),
                i.getRotation(),
                AnimationScript.center(i, true),
                i.getCell(),
                i.isVisible(),
                tags), utils.getString(R.string.title_itemInfo));
    }

    private Map<String, String> getTags(Item item) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            String s = new StreamReader(utils.getLightningContext().getFilesDir().getPath() + "/pages/" + item.getParent().getId() + "/items").read();
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
                Toast.makeText(utils.getLightningContext(), utils.getString(R.string.toast_noTags), Toast.LENGTH_SHORT).show();
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
            String s = new StreamReader(utils.getLightningContext().getFilesDir().getPath() + "/pages/" + container.getId() + "/conf").read();
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
        String tags = StreamSupport.stream(getTags(c).entrySet()).map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining("\n"));
        String name;
        switch (t) {
            case Container.TYPE_DESKTOP:
                name = ProxyFactory.cast(c, Desktop.class).getName();
                break;
            case Container.TYPE_FOLDER:
                name = ProxyFactory.cast(c.getOpener(), Shortcut.class).getLabel();
                break;
            default:
                name = c.getOpener().getName();
                break;
        }
        showText(utils.getString(R.string.text_containerInfo,
                t,
                name,
                c.getId(),
                c.getWidth(),
                c.getHeight(),
                c.getBoundingBox(),
                c.getCellWidth(),
                c.getCellHeight(),
                c.getPositionX(),
                c.getPositionY(),
                c.getPositionScale(),
                tags,
                Arrays.toString(c.getAllItems())), utils.getString(R.string.title_containerInfo));
    }

    private void showEventInfo(AlertDialog dialog) {
        dialog.dismiss();
        Event event = this.event;
        float touchX = 0;
        float touchY = 0;
        float touchScreenX = 0;
        float touchScreenY = 0;
        try {
            //test if event contains touch data
            touchX = event.getTouchX();
            touchY = event.getTouchY();
            touchScreenX = event.getTouchScreenX();
            touchScreenY = event.getTouchScreenY();
        } catch (Exception ignored) {
        }
        showText(utils.getString(R.string.text_eventInfo,
                event.getSource(),
                DateFormat.getInstance().format(event.getDate()),
                event.getContainer(),
                event.getScreen(),
                event.getItem(),
                event.getData(),
                touchX,
                touchY,
                touchScreenX,
                touchScreenY), utils.getString(R.string.title_eventInfo));
    }

    private void showText(String text, String title) {
        new AlertDialog.Builder(utils.getLightningContext()).setTitle(title)
                .setMessage(text)
                .setCancelable(true)
                .setNeutralButton(utils.getString(R.string.button_close), (dialog, which) -> dialog.dismiss())
                .show();
    }
}
