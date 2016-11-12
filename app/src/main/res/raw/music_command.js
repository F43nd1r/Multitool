var event = getEvent();
var sender = event.getContainer().my.connection.sender;
if (sender != null) {
    try {
        var msg = Message.obtain();
        msg.what = parseInt(event.getData());
        sender.send(msg);
    } catch (e) {}
}