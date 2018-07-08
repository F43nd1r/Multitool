var item = getEvent().getItem();
if (item != null && item.my.badgeListener != null) {
    try {
        item.my.badgeListener.unregister();
    } catch (e) {
    }
}