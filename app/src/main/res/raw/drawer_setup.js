var MY_PKG = "com.faendir.lightning_launcher.multitool";
// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id, name) {
    // use our package name to classify scripts
    var path = '/' + MY_PKG.replace(/\./g, '/') + "/drawer";

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

var screen = getActiveScreen();
var script = installScript("drawer", "AppDrawer");
var menu = installScript("drawer_menu", "Menu");
var size = 500;
var screen = getActiveScreen();
var d = getEvent().getContainer();
var panel = d.addPanel(0, 0, size, size);
var panelEditor = panel.getProperties().edit();
panelEditor.setBoolean("i.onGrid", false);
panelEditor.getBox("i.box").setColor("bl,br,bt,bb", "nfs", 0x00000000);
panelEditor.commit();
panel.setSize(size, size);
var p = panel.getContainer();
p.getProperties().edit()
    .setEventHandler("resumed", EventHandler.RUN_SCRIPT, script.getId())
    .setEventHandler("i.menu", EventHandler.RUN_SCRIPT, menu.getId())
    .setInteger("gridPColumnNum", 3)
    .setInteger("gridPRowNum", 2)
    .setInteger("gridLColumnNum", 3)
    .setInteger("gridLRowNum", 2)
    .commit();
bindClass("java.lang.Integer");
// use the last screen touch position, if any, as location for the new item
var x = screen.getLastTouchX();
var y = screen.getLastTouchY();
if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
    // no previous touch event, use a default position (can happen when using the hardware menu key for instance)
    x = size;
    y = size;
} else {
    // center around the touch position
    x -= size / 2;
    y -= size / 2;
}

panel.setPosition(x, y);