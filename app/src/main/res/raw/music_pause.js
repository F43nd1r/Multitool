var event = getEvent();
var panel = event.getContainer();
var sender = panel.my.connection.sender;
if (sender != null) {
    try {
        var msg = Message.obtain();
        msg.what = 4;
        msg.replyTo = panel.my.connection.receiver;
        sender.send(msg);
        var conn = panel.my.connection.conn;
        if(conn != null){
            getActiveScreen().getContext().unbindService(conn);
        }
    } catch (e) {}
}