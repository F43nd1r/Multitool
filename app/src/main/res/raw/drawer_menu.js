function bindPrefs(keys) {
    bindClass("android.database.Cursor");
    var resolver = getActiveScreen().getContext().getContentResolver();
    var result = {};
    var cursor = resolver.query(Uri.parse("content://com.faendir.lightning_launcher.multitool.provider/pref"), null, null, keys, null);
    while (cursor.moveToNext()) {
        result[cursor.getString(0)] = cursor.getString(1);
    }
    cursor.close();
    return result;
}

function setPref(key, value) {
    bindClass("android.database.Cursor");
    bindClass("android.content.ContentValues");
    var resolver = getActiveScreen().getContext().getContentResolver();
    var result = {};
    var values = new ContentValues();
    values.put(key,value);
    resolver.update(Uri.parse("content://com.faendir.lightning_launcher.multitool.provider/pref"), values, null, null);
}

var mode = menu.getMode();
if(mode == Menu.MODE_ITEM_NO_EM || mode == Menu.MODE_ITEM_EM) {
    menu.addMainItem("Hide", function(v) {
        hide();
        menu.close();
    });
}

function hide(){
    var name = item.getTag("intent");
    if(name != null){
        var prefs = bindPrefs(["hiddenApps"]);
        var hidden = JSON.parse(prefs.hiddenApps)||[];
        hidden.push(name);
        setPref("hiddenApps",JSON.stringify(hidden));
        item.getParent().removeItem(item);
        getActiveScreen().runScript("com/faendir/lightning_launcher/multitool/drawer","AppDrawer",null);
    }
}
