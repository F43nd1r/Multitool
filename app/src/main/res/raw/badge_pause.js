var event = getEvent();
var item = event.getItem();
if(item == null || item.my.badgeListener == null) return;
var badgeListener = item.my.badgeListener;
if (badgeListener != null) {
    try {
        badgeListener.unregister();
    } catch (e) {}
}