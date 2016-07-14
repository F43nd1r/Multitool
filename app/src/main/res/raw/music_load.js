LL.bindClass("android.content.ServiceConnection");
LL.bindClass("android.os.Messenger");
LL.bindClass("android.os.Message");
LL.bindClass("android.os.Handler");
LL.bindClass("java.lang.Exception");

var c = LL.getContext();
var data = LL.getEvent().getData();
var currentScript = LL.getCurrentScript();
if(data != null && data != ""){
currentScript.setTag(data);
}
var h = new JavaAdapter(Handler,{
handleMessage:function(msg){
try{
var bundle = msg.getData();
var albumArt = bundle.getParcelable("albumArt");
var title = bundle.getString("title");
var album = bundle.getString("album");
var artist = bundle.getString("artist");
if(albumArt != null){
var panel = LL.getContainerById(currentScript.getTag());
var item = panel.getItemByName("albumart");
var img = LL.createImage(albumArt.getWidth(),albumArt.getHeight());
img.draw().drawBitmap(albumArt,0,0,null);
//albumArt.recycle();
item.setBoxBackground(img,"nsf",true);
}
LL.getVariables().edit().setString("title",title).setString("album",album).setString("artist",artist).commit();
}catch(e){
new Exception(e).printStackTrace();
}
}});
multitoolMusicReceiver = new Messenger(h);
var conn = new ServiceConnection(){
onServiceConnected:function(name,binder){
multitoolMusicSender = new Messenger(binder);
},
onServiceDisconnected:function(name){}
};
var i = new Intent();
i.setClassName("com.faendir.lightning_launcher.multitool","com.faendir.lightning_launcher.multitool.music.MusicManager");
c.bindService(i,conn,Context.BIND_AUTO_CREATE);