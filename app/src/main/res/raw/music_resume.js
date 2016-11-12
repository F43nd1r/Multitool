bindClass("android.content.ServiceConnection");
bindClass("android.os.Messenger");
bindClass("android.os.Message");
bindClass("android.os.Handler");
bindClass("java.lang.Exception");
bindClass("android.graphics.Rect");
bindClass("android.util.Log");

var s = getActiveScreen();
var c = s.getContext();

function bindPrefs(keys) {
    bindClass("android.database.Cursor");
    var resolver = c.getContentResolver();
    var result = {};
    var cursor = resolver.query(Uri.parse("content://com.faendir.lightning_launcher.multitool.provider/pref"), null, null, keys, null);
    while (cursor.moveToNext()) {
        result[cursor.getString(0)] = cursor.getString(1);
    }
    cursor.close();
    return result;
}

var panel = getEvent().getContainer();
var h = new JavaAdapter(Handler, {
    handleMessage: function(msg) {
        try {
            var bundle = msg.getData();
            var albumArt = bundle.getParcelable("albumArt");
            var title = bundle.getString("title") || "";
            var album = bundle.getString("album") || "";
            var artist = bundle.getString("artist") || "";
            var player = bundle.getString("player") || "";
            var prefs = bindPrefs(["coverFillMode"]);
            var mode = parseInt(prefs.coverFillMode);
                var item = panel.getItemByName("albumart");
                var wi = item.getWidth();
                var hi = item.getHeight();
                var img = Image.createImage(wi, hi);
            if (albumArt != null) {
                var wa = albumArt.getWidth();
                var ha = albumArt.getHeight();
                var src, dest;
                switch (mode) {
                    case 0:
                        var factor = wa / ha * hi / wi;
                        var x, y;
                        if (factor < 1) {
                            x = 0;
                            y = (ha - ha * factor) / 2;
                        } else {
                            x = (wa - wa / factor) / 2;
                            y = 0;
                        }
                        src = new Rect(x, y, wa - x, ha - y);
                        dest = new Rect(0, 0, wi, hi);
                        break;
                    case 1:
                        var factor = wi / hi * ha / wa;
                        var x, y;
                        if (factor < 1) {
                            x = 0;
                            y = (hi - hi * factor) / 2;
                        } else {
                            x = (wi - wi / factor) / 2;
                            y = 0;
                        }
                        src = new Rect(0, 0, wa, ha);
                        dest = new Rect(x, y, wi - x, hi - y);
                        break;
                    case 2:
                        src = new Rect(0, 0, wa, ha);
                        dest = new Rect(0, 0, wi, hi);
                        break;
                }
                img.draw().drawBitmap(albumArt, src, dest, null);
            }
                item.setBoxBackground(img, "nsf", false);
            getVariables().edit().setString("title", title).setString("album", album).setString("artist", artist).setString("player",player).commit();
        } catch (e) {
            new Exception(e).printStackTrace();
        }
    }
});
panel.my.connection = panel.extend();
panel.my.connection.receiver = new Messenger(h);
panel.my.connection.conn = new ServiceConnection() {
    onServiceConnected: function(name, binder) {
        panel.my.connection.sender = new Messenger(binder);
            var msg = Message.obtain();
            msg.what = 3;
            msg.replyTo = panel.my.connection.receiver;
            panel.my.connection.sender.send(msg);
    },
    onServiceDisconnected: function(name) {}
};
var i = new Intent();
i.setClassName("com.faendir.lightning_launcher.multitool", "com.faendir.lightning_launcher.multitool.music.MusicManager");
c.bindService(i, panel.my.connection.conn, Context.BIND_AUTO_CREATE);