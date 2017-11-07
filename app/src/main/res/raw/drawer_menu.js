eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

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
