eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

item.setHorizontalGrab(true);
item.setVerticalGrab(true);

var context = getActiveScreen().getContext();
try{
    // noinspection JSAnnotator
    return getObjectFactory().constructLightningGestureView();
}catch(e){
    bindClass("android.util.Log");
    bindClass("android.widget.TextView");
    Log.w("[MULTITOOL]", "Failed to load gesture widget");
    var t = new TextView(context);
    t.setText("Unable to load gesture widget.\nPlease restart Lightning Launcher.");
    // noinspection JSAnnotator
    return t;
}