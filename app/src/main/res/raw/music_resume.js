eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

var panel = getEvent().getContainer();
panel.my.musicListener = getObjectFactory().constructMusicListener();
panel.my.musicListener.onChange(false);
panel.my.musicListener.register();