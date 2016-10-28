var MY_PKG = "com.faendir.lightning_launcher.multitool";
// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id, name, pathSuffix) {
    // use our package name to classify scripts
    var path = '/' + MY_PKG.replace(/\./g, '/') + '/' + pathSuffix;

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

var e = getEvent();
var view = e.getContainer().addCustomView(e.getTouchX(), e.getTouchY());
var script = installScript("gesture", "Gesture Launcher", "gesture");
var editor = view.getProperties().edit();
editor.setString("v.onCreate", "" + script.getId());
editor.setString("i.selectionEffect", "PLAIN");
editor.getBox("i.box").setColor("c", "nsf", 0x42FfFfFf);
editor.commit();