if(typeof multitoolMusicSender != "undefined"){
try{
var msg = Message.obtain();
msg.what = parseInt(LL.getEvent().getData());
multitoolMusicSender.send(msg);
}catch(e){}
}