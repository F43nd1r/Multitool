var target = getEvent().getItem() || getEvent().getContainer();
if (target.my.listener != null) {
    try {
        target.my.listener.unregister();
    } catch (e) {
    }
}