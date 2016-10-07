var run = function() {
    if (typeof multitoolMusicSender == "undefined") {
        setTimeout(run, 50);
    } else {
        try {
            var msg = Message.obtain();
            msg.what = 3;
            msg.replyTo = multitoolMusicReceiver;
            multitoolMusicSender.send(msg);
        } catch (e) {}
    }
}
run();