eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

LL.bindClass("android.app.AlertDialog");

var script = installScript("animation","animation","Animation");
var container = getEvent().getContainer();
var prop = container.getProperties();
addEventHandler(prop, "posChanged", new EventHandler(EventHandler.RUN_SCRIPT, ""+script.getId()));
var tag = container.getTag("animation");
var config = tag != null ? JSON.parse(tag) : {animation:-1};
var context = getActiveScreen().getContext();
var builder = new AlertDialog.Builder(context);
builder.setTitle("Choose an animation style");
builder.setItems(["Bulldoze", "Card Style", "Flip", "Shrink"], function(dialog,which){
    config.animation = which;
    container.setTag("animation",JSON.stringify(config));
});
builder.setNegativeButton("Disable",function(dialog,which){
    config.animation = -1;
    container.setTag("animation",JSON.stringify(config));
});
builder.show();