eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

var target = getEvent().getItem() || getEvent().getContainer();
target.my.listener = getObjectFactory().get(getEvent().getData());
target.my.listener.register();