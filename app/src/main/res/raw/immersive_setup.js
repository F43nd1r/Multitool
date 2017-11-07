eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

var script = installScript("immersive","immersive","Toggle immersive mode");
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

