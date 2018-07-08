eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

var item = getEvent().getItem();
item.my.badgeListener = getObjectFactory().constructBadgeListener();
item.my.badgeListener.onChange(false);
item.my.badgeListener.register();