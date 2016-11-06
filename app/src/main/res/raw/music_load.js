bindClass("android.content.ServiceConnection");
bindClass("android.os.Messenger");
bindClass("android.os.Message");
bindClass("android.os.Handler");
bindClass("java.lang.Exception");
bindClass("android.graphics.Rect");

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

var data = getEvent().getData();
var currentScript = getCurrentScript();
if (data != null && data != "") {
    currentScript.setTag(data);
}
var h = new JavaAdapter(Handler, {
    handleMessage: function(msg) {
        try {
            var bundle = msg.getData();
            var albumArt = bundle.getParcelable("albumArt");
            var title = bundle.getString("title");
            var album = bundle.getString("album");
            var artist = bundle.getString("artist");
            var prefs = bindPrefs(["coverFillMode"]);
            var mode = parseInt(prefs.coverFillMode);
            if (albumArt != null) {
                var panel = s.getContainerById(currentScript.getTag());
                var item = panel.getItemByName("albumart");
                var wi = item.getWidth();
                var hi = item.getHeight();
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
                var img = Image.createImage(wi, hi);
                img.draw().drawBitmap(albumArt, src, dest, null);
                item.setBoxBackground(img, "nsf", false);
            }
            getVariables().edit().setString("title", title).setString("album", album).setString("artist", artist).commit();
        } catch (e) {
            new Exception(e).printStackTrace();
        }
    }
});
multitoolMusicReceiver = new Messenger(h);
var conn = new ServiceConnection() {
    onServiceConnected: function(name, binder) {
        multitoolMusicSender = new Messenger(binder);
    },
    onServiceDisconnected: function(name) {}
};
var i = new Intent();
i.setClassName("com.faendir.lightning_launcher.multitool", "com.faendir.lightning_launcher.multitool.music.MusicManager");
c.bindService(i, conn, Context.BIND_AUTO_CREATE);