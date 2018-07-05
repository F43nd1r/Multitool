eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

bindClass("android.os.Handler");
bindClass("android.graphics.Rect");

var panel = getEvent().getContainer();
panel.my.musicListener = getObjectFactory().constructMusicListener(getActiveScreen().getContext(), panel, LL);
panel.my.musicListener.onChange(false);
panel.my.musicListener.register();