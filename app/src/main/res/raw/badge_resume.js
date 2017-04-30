bindClass("android.content.ServiceConnection");
bindClass("android.util.Log");
bindClass("java.lang.Class");
bindClass("dalvik.system.PathClassLoader");

var MY_PKG = "com.faendir.lightning_launcher.multitool";

var s = getActiveScreen();
var c = s.getContext();

var multitool = c.createPackageContext("com.faendir.lightning_launcher.multitool", 2);
var apk = multitool.getPackageManager().getApplicationInfo("com.faendir.lightning_launcher.multitool", 0).sourceDir;
var clsLoader = new PathClassLoader(apk, PathClassLoader.getSystemClassLoader());
var badgeServiceClass = Class.forName("com.faendir.lightning_launcher.multitool.badge.IBadgeService$Stub", true, clsLoader);
var badgeListenerClass = Class.forName("com.faendir.lightning_launcher.multitool.badge.BadgeListener", true, clsLoader);

var item = getEvent().getItem();
item.my.connection = item.extend();
item.my.connection.listener = badgeListenerClass.newInstance();
item.my.connection.listener.setConsumer(function(newCount, packageName){
        if(packageName == item.getTag("package")){
            c.runOnUiThread(function(){item.setLabel(newCount + "");});
        }
    });
item.my.connection.conn = new ServiceConnection() {
    onServiceConnected: function(name, binder) {
        var asInterface = badgeServiceClass.getMethod("asInterface", Class.forName("android.os.IBinder"))
        item.my.connection.service = asInterface.invoke(null, binder);
        item.my.connection.service.registerListener(item.my.connection.listener);
    },
    onServiceDisconnected: function(name) {}
};
var i = new Intent();
i.setClassName("com.faendir.lightning_launcher.multitool", "com.faendir.lightning_launcher.multitool.badge.BadgeService");
c.bindService(i, item.my.connection.conn, Context.BIND_AUTO_CREATE);