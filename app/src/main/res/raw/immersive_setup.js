var MY_PKG = "com.faendir.lightning_launcher.multitool";
// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id, name) {
    // use our package name to classify scripts
    var path = '/' + MY_PKG.replace(/\./g, '/') + "/immersive";

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

var script = installScript("immersive","Toggle immersive mode");
var d = getActiveScreen().getCurrentDesktop();
var p = d.getProperties();
var e = p.getEventHandler("resumed");
if(e.getAction() == EventHandler.RUN_SCRIPT && e.getData() == script.getId()){
    p.edit().setEventHandler("resumed", EventHandler.UNSET, null).commit();
    getActiveScreen().getContext().getWindow().getDecorView().setSystemUiVisibility(0);
} else {
    p.edit().setEventHandler("resumed", EventHandler.RUN_SCRIPT, script.getId()).commit();
    script.run(getActiveScreen(), null);
}

