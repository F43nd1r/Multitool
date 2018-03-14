eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

var item = getEvent().getItem();
var showZero = bindPrefs(["badgeShowZero"]).badgeShowZero == "true";

var listener = function(count){
    if (!showZero && count == 0) {
        item.setVisibility(false);
    } else {
        item.setLabel(count + "");
        item.setVisibility(true);
    }
}
item.my.badgeListener = getObjectFactory().constructBadgeListener(new Handler(), getActiveScreen().getContext(), item.getTag("package"), listener);
item.my.badgeListener.onChange(false);
item.my.badgeListener.register();