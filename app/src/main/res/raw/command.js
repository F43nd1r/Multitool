var target = getEvent().getItem() || getEvent().getContainer();
if (target.my.listener != null) {
    try {
        target.my.listener.handleCommand(getEvent().getData());
    } catch (e) {
    }
}