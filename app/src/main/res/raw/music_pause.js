if (typeof multitoolMusicSender != "undefined") {
    try {
        var msg = Message.obtain();
        msg.what = 4;
        msg.replyTo = multitoolMusicReceiver;
        multitoolMusicSender.send(msg);
    } catch (e) {}
}