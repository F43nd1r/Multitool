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

var player = getVariables().getString("player");
var screen = getActiveScreen();
try {
    if (player == null) {
        var prefs = bindPrefs(["musicDefaultPackage"]);
        player = prefs.musicDefaultPackage;
    }
    var intent = screen.getContext().getPackageManager().getLaunchIntentForPackage(player);
    screen.startActivity(intent);
} catch (ignored) {}