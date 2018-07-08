eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

bindClass("android.os.Handler");

var item = getEvent().getItem();
item.my.badgeListener = getObjectFactory().constructBadgeListener(javaEval);
item.my.badgeListener.onChange(false);
item.my.badgeListener.register();