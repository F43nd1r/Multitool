bindClass("java.lang.Integer");
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
var load = installScript("music_load", "load", "music");
var resume = installScript("music_resume", "resume", "music");
var pause = installScript("music_pause", "pause", "music");
var command = installScript("music_command", "command", "music");
var size = 500;
var screen = getActiveScreen();
var d = screen.getCurrentDesktop();
var panel = d.addPanel(0, 0, size, size);
var panelEditor = panel.getProperties().edit();
panelEditor.setBoolean("i.onGrid", false);
panelEditor.getBox("i.box").setColor("bl,br,bt,bb", "nfs", 0x00000000);
panelEditor.commit();
panel.setSize(size, size);
var p = panel.getContainer();
p.getProperties().edit().setEventHandler("load", EventHandler.RUN_SCRIPT, load.getId()).setEventHandler("resumed", EventHandler.RUN_SCRIPT, resume.getId())
    .setEventHandler("paused", EventHandler.RUN_SCRIPT, pause.getId()).setString("scrollingDirection", "NONE").commit();
var albumart = p.addShortcut("albumart", new Intent(), 0, 0);
albumart.setName("albumart");
var albumartEditor = albumart.getProperties().edit();
albumartEditor.setBoolean("s.labelVisibility", false).setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).setBoolean("i.onGrid", false);
albumartEditor.getBox("i.box").setColor("c", "s", 0xffffffff);
albumartEditor.commit();
var title = p.addShortcut("title", new Intent(), 0, 0);
title.getProperties().edit().setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).setBoolean("i.onGrid", false).commit();
title.setBinding("s.label", "$title", true);
var album = p.addShortcut("album", new Intent(), 0, 0);
album.getProperties().edit().setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).setBoolean("i.onGrid", false).commit();
album.setBinding("s.label", "$album", true);
var artist = p.addShortcut("artist", new Intent(), 0, 0);
artist.getProperties().edit().setBoolean("s.iconVisibility", false).setBoolean("i.enabled", false).setBoolean("i.onGrid", false).commit();
artist.setBinding("s.label", "$artist", true);
var play = p.addShortcut("Play/Pause", new Intent(), 0, 0);
play.getProperties().edit().setBoolean("s.labelVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, command.getId() + "/5").setBoolean("i.onGrid", false).commit();
play.setDefaultIcon(Image.createImage(MY_PKG, "ic_play"));
var next = p.addShortcut("Next", new Intent(), 0, 0);
next.getProperties().edit().setBoolean("s.labelVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, command.getId() + "/6").setBoolean("i.onGrid", false).commit();
next.setDefaultIcon(Image.createImage(MY_PKG, "ic_next"));
var previous = p.addShortcut("Previous", new Intent(), 0, 0);
previous.getProperties().edit().setBoolean("s.labelVisibility", false).setEventHandler("i.tap", EventHandler.RUN_SCRIPT, command.getId() + "/7").setBoolean("i.onGrid", false).commit();
previous.setDefaultIcon(Image.createImage(MY_PKG, "ic_previous"));
albumart.setPosition(0, 0);
albumart.setSize(size, size);
title.setPosition(0, 0);
title.setSize(size, 50);
album.setPosition(0, 50);
album.setSize(size, 50);
artist.setPosition(0, 100);
artist.setSize(size, 50);
play.setPosition(150, 300);
play.setSize(200, 200);
next.setPosition(350, 300);
next.setSize(150, 200);
previous.setPosition(0, 300);
previous.setSize(150, 200);
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