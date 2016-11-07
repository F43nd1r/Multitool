var player = getVariables().getString("player");
var screen = getActiveScreen();
try{
    var intent = screen.getContext().getPackageManager().getLaunchIntentForPackage(player);
    screen.startActivity(intent);
} catch(ignored){}