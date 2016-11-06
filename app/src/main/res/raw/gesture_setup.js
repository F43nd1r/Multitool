var MY_PKG = "com.faendir.lightning_launcher.multitool";
// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id, name) {
    // use our package name to classify scripts
    var path = '/' + MY_PKG.replace(/\./g, '/') + "/gesture";

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
var view = getEvent().getContainer().addCustomView(screen.getLastTouchX(), screen.getLastTouchY());
var script = installScript("gesture", "Gesture Launcher");
var menu = installScript("gesture_menu", "Menu");
var editor = view.getProperties().edit();
editor.setString("v.onCreate", "" + script.getId());
editor.setString("i.selectionEffect", "PLAIN");
editor.getBox("i.box").setColor("c", "nsf", 0x42FfFfFf);
editor.setEventHandler("i.menu", EventHandler.RUN_SCRIPT, menu.getId());
editor.commit();