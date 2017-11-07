eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

bindClass("android.os.Handler");
bindClass("android.graphics.Rect");

var panel = getEvent().getContainer();
var listener = function(titleInfo) {
        try {
            var albumArt = titleInfo.getAlbumArt();
            var title = titleInfo.getTitle();
            var album = titleInfo.getAlbum();
            var artist = titleInfo.getArtist();
            var player = titleInfo.getPackageName();
            var prefs = bindPrefs(["coverFillMode"]);
            var mode = parseInt(prefs.coverFillMode.replace("\"",""));
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
            e.javaException.printStackTrace();
        }
};
panel.my.musicListener = getObjectFactory().constructMusicListener(new Handler(), c, listener);
panel.my.musicListener.onChange(false);
panel.my.musicListener.register();