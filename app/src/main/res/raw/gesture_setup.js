eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

var screen = getActiveScreen();
var view = getEvent().getContainer().addCustomView(screen.getLastTouchX(), screen.getLastTouchY());
var script = installScript("gesture", "gesture", "Gesture Launcher");
var menu = installScript("gesture", "gesture_menu", "Menu");
var editor = view.getProperties().edit();
editor.setString("v.onCreate", "" + script.getId());
editor.setString("i.selectionEffect", "PLAIN");
editor.getBox("i.box").setColor("c", "nsf", 0x42FfFfFf);
editor.setEventHandler("i.menu", EventHandler.RUN_SCRIPT, menu.getId());
editor.commit();