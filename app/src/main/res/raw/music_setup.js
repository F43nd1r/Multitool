bindClass("java.lang.Integer");
var MY_PKG = "com.faendir.lightning_launcher.multitool";
// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id, name) {
    // use our package name to classify scripts
    var path = '/' + MY_PKG.replace(/\./g, '/') + "/music";

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
var load = installScript("music_load", "load");
var resume = installScript("music_resume", "resume");
var pause = installScript("music_pause", "pause");
var command = installScript("music_command", "command");
var launch = installScript("music_launch","launch");
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
    .setEventHandler("load", EventHandler.RUN_SCRIPT, load.getId())
    .setEventHandler("resumed", EventHandler.RUN_SCRIPT, resume.getId())
    .setEventHandler("paused", EventHandler.RUN_SCRIPT, pause.getId())
    .setString("scrollingDirection", "NONE")
    .setInteger("gridPColumnNum", 3)
    .setInteger("gridPRowNum", 10)
    .setInteger("gridLColumnNum", 3)
    .setInteger("gridLRowNum", 10)
    .commit();
var albumart = p.addShortcut("albumart", new Intent(), 0, 0);
albumart.setName("albumart");
var title = p.addShortcut("title", new Intent(), 0, 0);
var album = p.addShortcut("album", new Intent(), 0, 0);
var artist = p.addShortcut("artist", new Intent(), 0, 0);
var play = p.addShortcut("Play/Pause", new Intent(), 0, 0);
var next = p.addShortcut("Next", new Intent(), 0, 0);
var previous = p.addShortcut("Previous", new Intent(), 0, 0);
var albumartEditor = albumart.getProperties().edit();
albumartEditor.setBoolean("s.labelVisibility", false).setBoolean("s.iconVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, launch.getId());
albumartEditor.getBox("i.box").setColor("c", "s", 0xffffffff);
albumartEditor.commit();
title.getProperties().edit().setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).commit();
album.getProperties().edit().setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).commit();
artist.getProperties().edit().setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).commit();
play.getProperties().edit().setBoolean("s.labelVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, command.getId() + "/5").commit();
play.setDefaultIcon(Image.createImage(MY_PKG, "ic_play"));
next.getProperties().edit().setBoolean("s.labelVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, command.getId() + "/6").commit();
next.setDefaultIcon(Image.createImage(MY_PKG, "ic_next"));
previous.getProperties().edit().setBoolean("s.labelVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, command.getId() + "/7").commit();
previous.setDefaultIcon(Image.createImage(MY_PKG, "ic_previous"));
title.setBinding("s.label", "$title", true);
album.setBinding("s.label", "$album", true);
artist.setBinding("s.label", "$artist", true);
albumart.setCell(0, 0, 3, 10, true);
title.setCell(0, 0, 3, 1, true);
album.setCell(0, 1, 3, 2, true);
artist.setCell(0, 2, 3, 3, true);
play.setCell(1, 7, 2, 10, true);
next.setCell(2, 7, 3, 10, true);
previous.setCell(0, 7, 1, 10, true);
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
screen.runScript(load.getName(), p.getId());
screen.runScript(resume.getName(), null);