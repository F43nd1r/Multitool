var event = getEvent();
var panel = event.getContainer();
var musicListener = panel.my.musicListener;
if (musicListener != null) {
    try {
        musicListener.unregister();
    } catch (e) {}
}