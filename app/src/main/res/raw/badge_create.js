eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

function createBadge(package) {
    var d = getActiveScreen().getContainerById(token);
    var resume = getScriptByPathAndName(getCurrentScript().getPath(), "resume");
    var pause = getScriptByPathAndName(getCurrentScript().getPath(), "pause");
    var item = d.addShortcut("0", new Intent(), 0, 0);
    item.setTag("package", package);
    item.getProperties().edit().setBoolean("i.onGrid", false).setBoolean("s.iconVisibility", false).setBoolean("s.labelVisibility", true).setBoolean("i.enabled",false)
        .setEventHandler("i.resumed",EventHandler.RUN_SCRIPT, resume.getId()).setEventHandler("i.paused",EventHandler.RUN_SCRIPT, pause.getId()).commit();
    centerOnTouch(item);
}

if(resultCode==-1){
    createBadge(data.getParcelableExtra(Intent.EXTRA_INTENT).getComponent().getPackageName());
}