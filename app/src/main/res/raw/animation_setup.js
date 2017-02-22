LL.bindClass("android.app.AlertDialog");
var MY_PKG = "com.faendir.lightning_launcher.multitool";
// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id, name) {
    // use our package name to classify scripts
    var path = '/' + MY_PKG.replace(/\./g, '/') + "/animation";

    // load the script (if any) among the existing ones
    var script = getScriptByPathAndName(path, name);

    // load the script text from the package
    var script_text = loadRawResource(MY_PKG, id);

    if (script == null) {
        // script not found: install it
        script = createScript(path, name, script_text, 0);
    } else {
        // the script already exists: update its text
        script.setText(script_text);
    }
    return script;
}

var script = installScript("animation","Animation");
var container = getEvent().getContainer();
var prop = container.getProperties();
prop.edit().setEventHandler("posChanged", new EventHandler(EventHandler.RUN_SCRIPT, ""+script.getId(), prop.getEventHandler("posChanged"))).commit();
var tag = container.getTag("animation");
var config = tag != null ? JSON.parse(tag) : {animation:-1};
var context = getActiveScreen().getContext();
var builder = new AlertDialog.Builder(context);
builder.setTitle("Choose an animation style");
builder.setItems(["Bulldoze", "Card Style", "Flip", "Shrink"], function(dialog,which){
    config.animation = which;
    container.setTag("animation",JSON.stringify(config));
});
builder.setNegativeButton("Disable",function(dialog,which){
    config.animation = -1;
    container.setTag("animation",JSON.stringify(config));
});
builder.show();