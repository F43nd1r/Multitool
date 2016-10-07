LL.bindClass("java.lang.Class");
LL.bindClass("dalvik.system.PathClassLoader");

item.setHorizontalGrab(true);
item.setVerticalGrab(true);

var c = LL.getContext().createPackageContext("com.faendir.lightning_launcher.multitool", 2);
var apk = c.getPackageManager().getApplicationInfo("com.faendir.lightning_launcher.multitool", 0).sourceDir;
var clsLoader = new PathClassLoader(apk, PathClassLoader.getSystemClassLoader());
var cls = Class.forName("com.faendir.lightning_launcher.multitool.gesture.LightningGestureView", true, clsLoader);
var v = cls.getConstructors()[0].newInstance(c);
return v;