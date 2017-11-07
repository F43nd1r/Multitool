eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

var screen = getActiveScreen();
var script = installScript("drawer", "drawer", "AppDrawer");
var menu = installScript("drawer", "drawer_menu", "Menu");
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
centerOnTouch(panel);