var MY_PKG="com.faendir.lightning_launcher.multitool";
// install (or update) a script given its id in the package, and its clear name in the launcher data
function installScript(id,name){
	// load the script (if any) among the existing ones
	var script=LL.getScriptByName(name);

	var script_text=LL.loadRawResource(MY_PKG,id);

	if(script==null){
		// script not found: install it
		script=LL.createScript(name,script_text,0);
	}else{
		// the script already exists: update its text
		script.setText(script_text);
	}
	return script;
}
var load = installScript("music_load","multitool_music_load");
var resume = installScript("music_resume","multitool_music_resume");
var pause = installScript("music_pause","multitool_music_pause");
var command = installScript("music_command","multitool_music_command");
var d = LL.getCurrentDesktop();
var panel = d.addPanel(0,0,500,500);
panel.getProperties().edit().setBoolean("i.onGrid",false).commit();
panel.setSize(500,500);
var p = panel.getContainer();
p.getProperties().edit().setEventHandler("load",EventHandler.RUN_SCRIPT,load.getId()).setEventHandler("resumed",EventHandler.RUN_SCRIPT,resume.getId()).setEventHandler("paused",EventHandler.RUN_SCRIPT,pause.getId()).commit();
var albumart = p.addShortcut("albumart",new Intent(),0,0);
albumart.setName("albumart");
albumart.getProperties().edit().setBoolean("s.labelVisibility",false).setBoolean("s.iconVisibility",false).setBoolean("i.enabled",false).setBoolean("i.onGrid",false).commit();
var title = p.addShortcut("title",new Intent(),0,0);
title.getProperties().edit().setBoolean("s.iconVisibility",false).setBoolean("i.enabled",false).setBoolean("i.onGrid",false).commit();
title.setBinding("s.label","$title",true);
var album = p.addShortcut("album",new Intent(),0,0);
album.getProperties().edit().setBoolean("s.iconVisibility",false).setBoolean("i.enabled",false).setBoolean("i.onGrid",false).commit();
album.setBinding("s.label","$album",true);
var artist = p.addShortcut("artist",new Intent(),0,0);
artist.getProperties().edit().setBoolean("s.iconVisibility",false).setBoolean("i.enabled",false).setBoolean("i.onGrid",false).commit();
artist.setBinding("s.label","$artist",true);
var play = p.addShortcut("Play/Pause", new Intent(),0,0);
play.getProperties().edit().setBoolean("s.iconVisibility",false).setEventHandler("i.tap",EventHandler.RUN_SCRIPT, command.getId()+"/5").setBoolean("i.onGrid",false).commit();
var next = p.addShortcut("Next", new Intent(),0,0);
next.getProperties().edit().setBoolean("s.iconVisibility",false).setEventHandler("i.tap",EventHandler.RUN_SCRIPT, command.getId()+"/6").setBoolean("i.onGrid",false).commit();
var previous = p.addShortcut("Previous", new Intent(),0,0);
previous.getProperties().edit().setBoolean("s.iconVisibility",false).setEventHandler("i.tap",EventHandler.RUN_SCRIPT, command.getId()+"/7").setBoolean("i.onGrid",false).commit();
albumart.setPosition(0,0);
albumart.setSize(500,500);
title.setPosition(0,0);
title.setSize(500, 50);
album.setPosition(0,50);
album.setSize(500,50);
artist.setPosition(0,100);
artist.setSize(500,50);
play.setPosition(150, 450);
play.setSize(200, 50);
next.setPosition(350,450);
next.setSize(150,50);
previous.setPosition(0,450);
previous.setSize(150,50);
LL.runScript(load.getName(),p.getId());
LL.runScript(resume.getName(),null);
LL.deleteScript(LL.getCurrentScript());
