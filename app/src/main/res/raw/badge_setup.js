eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

LL.bindClass("android.app.AlertDialog");

var resume = installScript("badge", "badge_resume", "resume");
var pause = installScript("badge", "badge_pause", "pause");
var screen = getActiveScreen();
var d = getEvent().getContainer();
var context = screen.getContext();
var builder = new AlertDialog.Builder(context);
builder.setTitle("For which app do you want to create a badge?");
builder.setItems(["Whatsapp", "Facebook", "Facebook Messenger", "Telegram", "Skype"], function(dialog,which){
    switch(which){
        case 0:
            createBadge("com.whatsapp");
            break;
        case 1:
            createBadge("com.facebook.katana");
            break;
        case 2:
            createBadge("com.facebook.orca");
            break;
        case 3:
            createBadge("org.telegram.messenger");
            break;
        case 4:
            createBadge("com.skype.raider");
            break;
    }
});
builder.setNegativeButton("Cancel",null);
builder.show();

function createBadge(package) {
    var item = d.addShortcut("0", new Intent(), 0, 0);
    item.setTag("package", package);
    item.getProperties().edit().setBoolean("i.onGrid", false).setBoolean("s.iconVisibility", false).setBoolean("s.labelVisibility", true).setBoolean("i.enabled",false)
        .setEventHandler("i.resumed",EventHandler.RUN_SCRIPT, resume.getId()).setEventHandler("i.paused",EventHandler.RUN_SCRIPT, pause.getId()).commit();
    centerOnTouch(item);
}