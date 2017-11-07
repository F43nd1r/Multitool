eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

bindClass("android.os.Handler");

var item = getEvent().getItem();

var listener = function(count){
    item.setLabel(count + "");
}
item.my.badgeListener = getObjectFactory().constructBadgeListener(new Handler(), getActiveScreen().getContext(), item.getTag("package"), listener);
item.my.badgeListener.onChange(false);
item.my.badgeListener.register();