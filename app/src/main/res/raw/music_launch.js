eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

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