eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

bindClass("android.os.Handler");

var item = getEvent().getItem();
var showZero = bindPrefs(["badgeShowZero"]).badgeShowZero == "true";

var listener = function(count){
    if (!showZero && count == 0) {
        item.setLabel("");
    } else {
        item.setLabel(count + "");
    }
}
item.my.badgeListener = getObjectFactory().constructBadgeListener(new Handler(), getActiveScreen().getContext(), item.getTag("package"), listener);
item.my.badgeListener.onChange(false);
item.my.badgeListener.register();