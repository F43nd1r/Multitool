LL.bindClass("android.content.ServiceConnection");
LL.bindClass("android.os.Messenger");
LL.bindClass("android.os.Message");
LL.bindClass("android.os.Handler");
LL.bindClass("java.lang.Exception");
LL.bindClass("android.graphics.Rect");

var c = LL.getContext();
var data = LL.getEvent().getData();
var currentScript = LL.getCurrentScript();
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
            var mode = bundle.getInt("coverMode");
            if (albumArt != null) {
                var panel = LL.getContainerById(currentScript.getTag());
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
                var img = LL.createImage(wi, hi);
                img.draw().drawBitmap(albumArt, src, dest, null);
                //albumArt.recycle();
                item.setBoxBackground(img, "nsf", false);
            }
            LL.getVariables().edit().setString("title", title).setString("album", album).setString("artist", artist).commit();
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