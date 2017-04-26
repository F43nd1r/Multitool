bindClass("java.lang.Integer");
var MY_PKG = "com.faendir.lightning_launcher.multitool";
// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id, name) {
    // use our package name to classify scripts
    var path = '/' + MY_PKG.replace(/\./g, '/') + "/badge";

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
var resume = installScript("badge_resume", "resume");
var pause = installScript("badge_pause", "pause");
var screen = getActiveScreen();
var d = getEvent().getContainer();
var item = d.addShortcut("0", new Intent(), 0, 0);
item.getProperties().edit().setBoolean("i.onGrid", false).setBoolean("s.iconVisibility", false).setBoolean("s.labelVisibility", true).setBoolean("i.enabled",false)
    .setEventHandler("i.resumed",EventHandler.RUN_SCRIPT, resume.getId()).setEventHandler("i.paused",EventHandler.RUN_SCRIPT, pause.getId()).commit();
// use the last screen touch position, if any, as location for the new item
var x = screen.getLastTouchX();
var y = screen.getLastTouchY();
var size = item.getWidth();
if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
    // no previous touch event, use a default position (can happen when using the hardware menu key for instance)
    x = size;
    y = size;
} else {
    // center around the touch position
    x -= size / 2;
    y -= size / 2;
}
item.setPosition(x, y);