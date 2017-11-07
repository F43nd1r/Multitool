eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

var load = installScript("music", "music_load", "load");
var resume = installScript("music", "music_resume", "resume");
var pause = installScript("music", "music_pause", "pause");
var command = installScript("music", "music_command", "command");
var launch = installScript("music", "music_launch","launch");
getActiveScreen().runAction(EventHandler.RESTART, null);
Toast.makeText(getActiveScreen().getContext(),"Done",Toast.LENGTH_SHORT).show();