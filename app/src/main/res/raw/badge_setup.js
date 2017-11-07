eval(loadRawResource("com.faendir.lightning_launcher.multitool","library"));

var resume = installScript("badge", "badge_resume", "resume");
var pause = installScript("badge", "badge_pause", "pause");
var create = installScript("badge", "badge_create", "create");
var screen = getActiveScreen();
var d = getEvent().getContainer();
var context = screen.getContext();
var intent = new Intent();
intent.setClassName(getMultiToolPackage(), getMultiToolPackage() + ".badge.AppChooser");
screen.startActivityForResult(intent, create, d.getId());