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
var load = installScript("music_load", "multitool_music_load");
var resume = installScript("music_resume", "multitool_music_resume");
var pause = installScript("music_pause", "multitool_music_pause");
var command = installScript("music_command", "multitool_music_command");
Toast.makeText(LL.getContext(),"Done",Toast.LENGTH_SHORT).show();