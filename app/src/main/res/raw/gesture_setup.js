var MY_PKG = "com.faendir.lightning_launcher.multitool";
// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id, name) {
    // load the script (if any) among the existing ones
    var script = LL.getScriptByName(name);

    var script_text = LL.loadRawResource(MY_PKG, id);

    if (script == null) {
        // script not found: install it
        script = LL.createScript(name, script_text, 0);
    } else {
        // the script already exists: update its text
        script.setText(script_text);
    }
    return script;
}

var e = LL.getEvent();
var view = e.getContainer().addCustomView(e.getTouchX(), e.getTouchY());
var script = installScript("gesture", "Gesture Launcher");
var editor = view.getProperties().edit();
editor.setString("v.onCreate", "" + script.getId());
editor.setString("i.selectionEffect", "PLAIN");
editor.getBox("i.box").setColor("c", "nsf", 0x42FfFfFf);
editor.commit();
Toast.makeText(LL.getContext(), "Done", Toast.LENGTH_SHORT).show();