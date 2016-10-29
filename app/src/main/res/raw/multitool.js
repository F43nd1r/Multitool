//Created by Lukas Morawietz in collaboration with TrianguloY
//import java classes
LL.bindClass("android.app.AlertDialog");
LL.bindClass("android.app.ProgressDialog");
LL.bindClass("android.content.DialogInterface");
LL.bindClass("android.os.Environment");
LL.bindClass("android.R");
LL.bindClass("android.widget.ExpandableListView");
LL.bindClass("android.widget.ImageView");
LL.bindClass("android.widget.LinearLayout");
LL.bindClass("android.widget.ListView");
LL.bindClass("android.widget.NumberPicker");
LL.bindClass("android.widget.SimpleAdapter");
LL.bindClass("android.widget.SimpleExpandableListAdapter");
LL.bindClass("android.widget.ScrollView");
LL.bindClass("android.widget.TextView");
LL.bindClass("java.io.File");
LL.bindClass("java.io.BufferedReader");
LL.bindClass("java.io.FileReader");
LL.bindClass("java.io.FileWriter");
LL.bindClass("java.util.HashMap");
LL.bindClass("java.util.ArrayList");


var hasItem = (LL.getEvent().getItem() != null);

//define Strings to display
var title = "What do you want to do?";
var items = []
var info = ["Information", []];
info[1].push("Event");
info[1].push("Container");
if (hasItem) {
    info[1].push("Item");
    info[1].push("Intent");
    info[1].push("Icon");
}
var itemUtils = ["Item Utilities", []];
itemUtils[1].push("Attach/Detach all Items");
itemUtils[1].push("Resize all detached Items");
itemUtils[1].push("Delete all Items");
itemUtils[1].push("Move Pages");
var other = ["Other", []];
other[1].push("Reset Tag");
other[1].push("Reset Tool");
other[1].push("Save changes");
other[1].push("Delete recent app history");
items.push(info);
items.push(itemUtils);
items.push(other);


expandableList(items, mainOnClick, title);

//handle user selection
function mainOnClick(groupPosition, childPosition) {
    switch (groupPosition) {
        case 0: //Information
            switch (childPosition) {
                case 0: //Event related
                    eventData();
                    break;
                case 1: //container related
                    containerData()
                    break;
                case 2: //item related
                    itemData();
                    break;
                case 3: //intent
                    intentData();
                    break;﻿
                case 4: //icon
                    iconData();
                    break;
            }
            break;
        case 1: //item utilities
            switch (childPosition) {
                case 0: //Attach/Detach all items
                    attachDetachAll();
                    break;
                case 1: //resize detached items
                    resizeAllDetached();
                    break;
                case 2: //delete items
                    deleteAll();
                    break;
                case 3: //move pages
                    movePages();
                    break;
            }
            break;
        case 2: //other
            switch (childPosition) {
                case 0: //reset Tag
                    resetTags();
                    break;
                case 1: //reset tool by trianguloY, ask him how it works :D
                    resetTool();
                    break;
                case 2:
                    saveLayout();
                    break;
                case 3:
                    resetRecents();
                    break;
            }
            break;
    }
}


function eventData() {
    var e = LL.getEvent();
    try { //test if event contains touch data
        e.getTouchScreenX();
        var ok = true;
    } catch (Exception) {
        var ok = false;
    }
    text("Source: " + e.getSource() + "\nDate: " + e.getDate() + "\nContainer: " + e.getContainer() + "\nItem: " + e.getItem() + (ok ? ("\nTouch: " + e.getTouchX() + "," + e.getTouchY() + "\nTouch (Screen): " + e.getTouchScreenX() + "," + e.getTouchScreenY()) : ""), "Event Information");
}

function containerData() {
    var c = LL.getEvent().getContainer();
    var t = c.getType(); //Differentiate between Desktop and other containers

    //read Tags from launcher file
    var s = read(LL.getContext().getFilesDir().getPath() + "/pages/" + c.getId() + "/conf");
    var data = JSON.parse(s);
    var tags = "Default: " + c.getTag();
    for (property in data.tags)
        tags += "\n" + property + ": " + data.tags[property];
    text("Type: " + t + "\nName/Label: " + (t == "Desktop" ? c.getName() : c.getOpener().getLabel()) + "\nID: " + c.getId() + "\nSize: " + c.getWidth() + "," + c.getHeight() + "\nBoundingbox: " + c.getBoundingBox() + "\nCell Size: " + c.getCellWidth() + "," + c.getCellHeight() + "\nCurrent Position: " + c.getPositionX() + "," + c.getPositionY() + "\nCurrent Scale: " + c.getPositionScale() + "\nTags: " + tags + "\nItems: " + c.getItems(), "Container Information");
}

function itemData() {
    var i = LL.getEvent().getItem();
    if (i == null) //check if event contains item
        text("no item found", "Error 5");

    //read tags from launcher file
    var s = read(LL.getContext().getFilesDir().getPath() + "/pages/" + LL.getEvent().getContainer().getId() + "/items");
    var all = JSON.parse(s).i;
    var x;
    var item;
    for (x = 0; x < all.length; x++) {
        item = all[x];
        if (item.b == i.getId()) break;
    }
    if (x == all.length) {
        text("Can't find Tags", "Error 6");
        return;
    }
    var tags = "Default: " + i.getTag();
    for (property in item.an) {
        if (property == "_") continue;
        tags += "\n" + property + ": " + item.an[property];
    }
    text("Label: " + i.getLabel() + "\nType: " + i.getType() + "\nID: " + i.getId() + "\nSize: " + i.getWidth() + "," + i.getHeight() + "\nPosition: " + i.getPositionX() + "," + i.getPositionY() + "\nScale: " + i.getScaleX() + "," + i.getScaleY() + "\nAngle: " + i.getRotation() + "\nCenter: " + center(i) + ((i.getType() == "Shortcut" || i.getType() == "Folder") ? "\nIntent:" + i.getIntent() : "") + "\nTags: " + tags, "Item Information");
}

function intentData() {
    it = LL.getEvent().getItem();
    if (it == null || item.getType() != "Shortcut") text("No Intent found.", "Error 1");
    else text("Intent: " + it.getIntent() + "\nExtras: " + it.getIntent().getExtras(), "Intent Information");
}

function iconData() {
    it = LL.getEvent().getItem();
    //create view structure
    var root = new LinearLayout(LL.getContext());
    root.setOrientation(LinearLayout.VERTICAL);

    //check for all kinds of images in this item and add them to the view if there are any
    addImageIfNotNull(root, it.getBoxBackground("n"), "Normal Box Background");
    addImageIfNotNull(root, it.getBoxBackground("s"), "Selected Box Background");
    addImageIfNotNull(root, it.getBoxBackground("f"), "Focused Box Background");
    if (it.getType() == "Shortcut") {
        addImageIfNotNull(root, image = it.getDefaultIcon(), "Default Icon");
        addImageIfNotNull(root, image = it.getCustomIcon(), "Custom Icon");
    }
    if (root.getChildCount() > 0) { //at least one image found
        var scroll = new ScrollView(LL.getContext());
        scroll.addView(root);
        customDialog(scroll, "Icon");
    } else Android.makeNewToast("No Image Data available", true).show(); //no image found
}

function attachDetachAll() {
    var items = LL.getEvent().getContainer().getItems();
    var attachDetach = function(toGrid) {
        for (x = 0; x < items.length; x++) {
            var i = items.getAt(x);
            i.getProperties().edit().setBoolean("i.onGrid", toGrid).commit();
        }
        Android.makeNewToast("Done!", true).show();
    }
    var attach = function() {
        attachDetach(true);
    }
    var detach = function() {
        attachDetach(false);
    }
    chooser([function() {}, attach, detach], ["Cancel", "Attach", "Detach"], "Do you want to attach or detach all items?", "MultiTool");
}

function resizeAllDetached() {
    var linearLayout = new LinearLayout(LL.getContext());
    var c = LL.getEvent().getContainer();
    linearLayout.setOrientation(LinearLayout.VERTICAL);
    var widthText = new TextView(LL.getContext());
    widthText.setText("Width: ");
    linearLayout.addView(widthText);
    var widthPicker = new NumberPicker(LL.getContext());
    widthPicker.setMinValue(1);
    widthPicker.setMaxValue(9999);
    widthPicker.setValue(c.getCellWidth());
    linearLayout.addView(widthPicker);
    var heightText = new TextView(LL.getContext());
    heightText.setText("Height: ");
    linearLayout.addView(heightText);
    var heightPicker = new NumberPicker(LL.getContext());
    heightPicker.setMinValue(1);
    heightPicker.setMaxValue(9999);
    heightPicker.setValue(c.getCellHeight());
    linearLayout.addView(heightPicker);
    var onClick = function() {
        var s = [widthPicker.getValue(), heightPicker.getValue()];
        var items = c.getItems();
        for (var a = 0; a < items.length; a++)
            items.getAt(a).setSize(s[0], s[1]);
    }
    customConfirmDialog(linearLayout, "To which size?", onClick);
}

function deleteAll() {
    var f = function() {
        var c = LL.getEvent().getContainer();
        var i = c.getItems();
        for (a = 0; a < i.length; a++)
            c.removeItem(i.getAt(a));
    }
    chooser([function() {}, f], ["No", "Yes"], "Are you sure?", "Delete all items");
}

function movePages() {
    var cont = LL.getEvent().getContainer();
    var items = cont.getItems();
    var cWidth = cont.getWidth();
    var cHeight = cont.getHeight();
    var cellsFloatX = cWidth / cont.getCellWidth();
    var cellsFloatY = cHeight / cont.getCellHeight();
    var cellsX = Math.round(cellsFloatX);
    var cellsY = Math.round(cellsFloatY);
    var f = function() {
            try {
                //page(s) selection
                var s = prompt("Which page do you want to move? (* for all) input has to be x,y (e.g. *,* for all pages)", "").split(",");
                var move = JSON.parse("[\"" + s[0] + "\",\"" + s[1] + "\"]");
                var done = true;
            } catch (Exception) {
                var done = false;
            }
            //check for valid input
            if (!done || move == null || move[0] == null || (move[0] != "*" && isNaN(parseInt(move[0]))) || move[1] == null || (move[1] != "*" && isNaN(parseInt(move[1])))) {
                Android.makeNewToast("Invalid input", true).show();
                return;
            }
            //format to int if needed
            if (move[0] != "*") move[0] = parseInt(move[0]);
            if (move[1] != "*") move[1] = parseInt(move[1]);
            try {
                //user selection: destination
                var dist = JSON.parse("[" + prompt("How far do you want to move? input has to be x,y (e.g. 1,0 for one page right)", "") + "]");
                var done = true;
            } catch (Exception) {
                var done = false;
            }
            //check for valid input
            if (!done || dist == null || dist[0] == null || isNaN(dist[0]) || dist[1] == null || isNaN(dist[1])) {
                Android.makeNewToast("Invalid input", true).show();
                return;
            }
            if (dist[0] == 0 && dist[1] == 0) return; //if nothing to do, do nothing :P

            //do the movement
            for (var i = items.getLength() - 1; i >= 0; --i) {
                var item = items.getAt(i);
                var pos = [item.getPositionX(), item.getPositionY()];
                //check if item should be moved
                if ((move[0] == "*" || (pos[0] >= cWidth * move[0] && pos[0] < cWidth * (move[0] + 1))) && (move[1] == "*" || (pos[1] >= cHeight * move[1] && pos[1] < cHeight * (move[1] + 1)))) {
                    var prop = item.getProperties();

                    //handle pinned item
                    var xx = 1,
                        yy = 1;
                    var pinMode = prop.getString("i.pinMode");
                    if (pinMode[0] == "X") xx = 0;
                    if (pinMode.indexOf("Y") != -1) yy = 0;

                    //move it
                    if (prop.getBoolean("i.onGrid")) {
                        var cell = item.getCell();
                        item.setCell(cell.getLeft() + cellsX * dist[0] * xx, cell.getTop() + cellsY * dist[1] * yy, cell.getRight() + cellsX * dist[0] * xx, cell.getBottom() + cellsY * dist[1] * yy);
                    } else
                        item.setPosition(pos[0] + cWidth * dist[0] * xx, pos[1] + cHeight * dist[1] * yy);
                }
            }
            LL.save();
        }
        //check for safe cell sizes
    if (Math.abs(cellsFloatX - cellsX) > 0.00001 || Math.abs(cellsFloatY - cellsY) > 0.00001)
        chooser([function() {}, f]["No", "Yes"], "The cells don't fill the screen as an exact vertical and/or horizontal number.\nDo you want to continue?", "Warning");
    else f();
}

function resetTags() {
    var d = LL.getEvent().getContainer();
    var i = LL.getEvent().getItem();
    if (i != null) { //Items Tag
        //read tags from launcher file
        var s = read(LL.getContext().getFilesDir().getPath() + "/pages/" + LL.getEvent().getContainer().getId() + "/items");
        var all = JSON.parse(s).i;
        var x;
        var item;
        for (x = 0; x < all.length; x++) {
            item = all[x];
            if (item.b == i.getId()) break;
        }
        if (x == all.length) {
            text("Can't find Tags", "Error 6");
            return;
        }
        var tags = [];
        for (property in item.an)
            tags.push(property);

        //If there are no Tags, do nothing
        if (tags.length == 0) {
            text("No Tags found", "Error 7");
            return;
        }
        //Option to delete all tags added to list
        tags.unshift("All Tags");
        var onClick = function(dialog, id) {
                //delete all selected Tags
                alert(id + ": " + tags[id]);
                if (id == 0) tags.shift();
                else tags = [tags[id]];
                for (var y = 0; y < tags.length; y++)
                    i.setTag(tags[y].toString(), null);
                Android.makeNewToast("Deleting tag(s) done!", true).show();
                LL.save();
            }
            //ask user for selection
        list(tags, onClick, "Which Tag do you want to reset?");
    } else { //Container Tags
        //read Tags from launcher file
        var s = read(LL.getContext().getFilesDir().getPath() + "/pages/" + d.getId() + "/conf");
        var data = JSON.parse(s);
        var tags = [];
        for (property in data.tags)
            tags.push(property);
        //Add the default tag to the List
        if (data.tag != null) tags.push("_");
        //If there are no Tags, do nothing
        if (tags.length == 0) {
            text("No Tags found", "Error 7");
            return;
        }
        //Option to delete all tags added to list
        tags.unshift("All Tags");
        var onClick = function(dialog, id) {
                //delete all selected Tags
                if (id == 0) tags.shift();
                else tags = [tags[id]];
                for (var x = 0; x < tags.length; x++) {
                    if (tags[x] == "_") d.setTag(null);
                    else d.setTag(tags[x].toString(), null);
                }
                Android.makeNewToast("Deleting tag(s) done!", true).show();
                LL.save();
            }
            //ask user for selection
        list(tags, onClick, "Which Tag do you want to reset?");
    }
}

function resetTool() {
    var cont = LL.getEvent().getContainer();
    var items = cont.getItems();
    var listItems = ["Cell (only grid items) [0,0]", "Position (only free items) [0,0]", "Rotation (only free items) [0]", "Scale (only free items) [1,1]", "Skew (only free items) [0,0]", "Size (only free items) [cell size]", "Visibility [true]"];
    var listener = new DialogInterface.OnMultiChoiceClickListener() {
        onClick: function(dialog, which, isChecked) {
            bools[which] = isChecked;
        }
    };
    var bools = [false, false, false, false, false, false, false];
    var builder = new AlertDialog.Builder(LL.getContext());
    builder.setMultiChoiceItems(listItems, bools, listener);
    builder.setTitle("Reset");
    builder.setCancelable(true);
    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
        onClick: function(dialog, which) {
            dialog.dismiss();
            for (var i = 0; i < items.getLength(); ++i) {
                var t = items.getAt(i);
                if (bools[0]) t.setCell(0, 0, 1, 1);
                if (bools[1]) t.setPosition(0, 0);
                if (bools[2]) t.setRotation(0);
                if (bools[3]) t.setScale(1, 1);
                if (bools[4]) t.setSkew(0, 0);
                if (bools[5]) t.setSize(cont.getCellWidth(), cont.getCellHeight());
                if (bools[6]) t.setVisibility(true);
            }
        }
    });
    builder.show();
}

function saveLayout() {
    LL.save();
    Android.makeNewToast("Saved Layout", true).show();
}

function resetRecents() {
    new File(LL.getContext().getFilesDir().getPath() + "/statistics").delete();
    Android.makeNewToast("Recents resetted", true).show();
}

//function to display a grouped list where the user can select one item
//items should be an array containing arrays which first item is the group and the second item is an array of the items in this group
//onClickFunction has to have two arguments. first is group position, second is child position
function expandableList(items, onClickFunction, title) {
    var builder = new AlertDialog.Builder(LL.getContext());
    var view = new ExpandableListView(LL.getContext());

    //transform array of items into the correct format
    var groupData = new ArrayList();
    var childData = new ArrayList();
    for (var x = 0; x < items.length; x++) {
        var gd = new HashMap();
        gd.put("root", items[x][0]);
        var cd = new ArrayList();
        groupData.add(gd);
        for (var y = 0; y < items[x][1].length; y++) {
            var cdMap = new HashMap();
            cdMap.put("child", items[x][1][y]);
            cd.add(cdMap);
        }
        childData.add(cd);
    }
    var mContext = LL.getContext().createPackageContext("com.faendir.lightning_launcher.multitool", 0);
    var layoutId = mContext.getResources().getIdentifier("list_item", "layout", "com.faendir.lightning_launcher.multitool");
    //assign the items to an adapter
    var adapter = new SimpleExpandableListAdapter(mContext, groupData, layoutId, ["root"], [R.id.text1], childData, layoutId, ["child"], [R.id.text1]);

    //set function to run on Click to listener
    var listener = new ExpandableListView.OnChildClickListener() {
            onChildClick: function(parent, view, groupPosition, childPosition, id) {
                dialog.dismiss();
                setTimeout(function() {
                    onClickFunction(groupPosition, childPosition);
                }, 0);
                return true;
            }
        }
        //assign adapter and listener to listview
    view.setAdapter(adapter);
    view.setOnChildClickListener(listener);
    //finish building
    builder.setView(view);
    builder.setCancelable(true);
    builder.setTitle(title);
    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        onClick: function(dialog, id) {
            dialog.cancel();
        }
    });
    dialog = builder.create();﻿
    dialog.show();
}

//function to display a List in a Popup, where the user can select one item
function list(items, onClickFunction, title) {
    var builder = new AlertDialog.Builder(LL.getContext());
    var listener = new DialogInterface.OnClickListener() {
        onClick: function(dialog, which) {
            dialog.dismiss();
            setTimeout(function() {
                onClickFunction(dialog, which);
            }, 0);
            return true;
        }
    }
    builder.setItems(items, listener);
    builder.setCancelable(true);
    builder.setTitle(title);
    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        onClick: function(dialog, id) {
            dialog.cancel();
        }
    }); //it has a Cancel Button
    builder.show();
}

//function to display an alert like Dialog, but scrollable and with custom Title
function text(txt, title) {
    var builder = new AlertDialog.Builder(LL.getContext());
    builder.setMessage(txt);
    builder.setCancelable(true);
    builder.setTitle(title);
    builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
        onClick: function(dialog, id) {
            dialog.dismiss();
        }
    });
    builder.show();
}

function chooser(functions, texts, txt, title) {
    var builder = new AlertDialog.Builder(LL.getContext());
    builder.setMessage(txt);
    builder.setCancelable(true);
    builder.setTitle(title);
    builder.setNeutralButton(texts[0], new DialogInterface.OnClickListener() {
        onClick: function(dialog, id) {
            dialog.dismiss();
            setTimeout(functions[0], 0);
        }
    });
    if (functions.length > 1) builder.setNegativeButton(texts[1], new DialogInterface.OnClickListener() {
        onClick: function(dialog, id) {
            dialog.dismiss();
            setTimeout(functions[1], 0);
        }
    });
    if (functions.length > 2) builder.setPositiveButton(texts[2], new DialogInterface.OnClickListener() {
        onClick: function(dialog, id) {
            dialog.dismiss();
            setTimeout(functions[2], 0);
        }
    });
    builder.show();
}

function customDialog(view, title) {
    var builder = new AlertDialog.Builder(LL.getContext());
    builder.setView(view);
    builder.setCancelable(true);
    builder.setTitle(title);
    builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
        onClick: function(dialog, id) {
            dialog.dismiss();
        }
    });
    builder.show();
}

function customConfirmDialog(view, title, onPositiveFunction) {
    var builder = new AlertDialog.Builder(LL.getContext());
    builder.setView(view);
    builder.setCancelable(true);
    builder.setTitle(title);
    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        onClick: function(dialog, id) {
            dialog.dismiss();
        }
    });
    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
        onClick: function(dialog, id) {
            dialog.dismiss();
            setTimeout(onPositiveFunction, 0);
        }
    });
    builder.show();
}

//helper function for item related, compute the center of an item
function center(item) {
    var r = item.getRotation() * Math.PI / 180;
    var sin = Math.abs(Math.sin(r));
    var cos = Math.abs(Math.cos(r));
    var w = item.getWidth() * item.getScaleX();
    var h = item.getHeight() * item.getScaleY();
    return [item.getPositionX() + (w * cos + h * sin) * 0.5, item.getPositionY() + (h * cos + w * sin) * 0.5];﻿
}﻿

//helper function for sorting labels
function noCaseSort(a, b) {
    if (a.toLowerCase() > b.toLowerCase()) return 1;
    if (a.toLowerCase() < b.toLowerCase()) return -1;
    return 0;
}

function read(filePath) {
    var file = new File(filePath);
    var r = new BufferedReader(new FileReader(file));
    var s = "";
    var l;
    while ((l = r.readLine()) != null) s += (l + "\n");
    return s;
}

function addImageIfNotNull(root, image, txt) {
    if (image != null) {
        var textView = new TextView(LL.getContext());
        textView.setText(txt + " (" + image.getWidth() + "x" + image.getHeight() + ")");
        root.addView(textView);
        var imageView = new ImageView(LL.getContext());
        imageView.setImageBitmap(image.getBitmap());
        root.addView(imageView);
    }
}